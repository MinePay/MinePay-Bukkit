package com.minepay.plugin.bukkit.task;

import com.minepay.plugin.bukkit.MinePayPlugin;
import com.minepay.plugin.bukkit.command.CommandTemplate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;

/**
 * Provides a netty based long-poll client which will perform an HTTP response and await a response
 * for an infinite amount of time.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@ChannelHandler.Sharable
public class CommandLongPollTask extends SimpleChannelInboundHandler<FullHttpResponse> implements Runnable {
    private final MinePayPlugin plugin;
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    public CommandLongPollTask(@Nonnull MinePayPlugin plugin) {
        this.plugin = plugin;
        this.workerGroup = new NioEventLoopGroup();

        this.bootstrap = new Bootstrap()
                .group(this.workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        tmf.init((KeyStore) null);

                        SSLContext context = SSLContext.getInstance("TLS");
                        context.init(null, tmf.getTrustManagers(), null);

                        SSLEngine engine = context.createSSLEngine("api.minepay.net", 443);
                        engine.setUseClientMode(true);

                        ch.pipeline()
                                .addLast(new SslHandler(engine))
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpContentDecompressor())
                                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                                .addLast(CommandLongPollTask.this);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void channelRead0(@Nonnull ChannelHandlerContext ctx, @Nonnull FullHttpResponse response) throws Exception {
        switch (response.getStatus().code()) {
            case 200:
                break;
            case 204:
                throw new IllegalStateException("Cannot access polling endpoint: No content");
            case 400:
            case 404:
                throw new IllegalStateException("Cannot access polling endpoint: Bad Request");
            default:
                throw new IllegalStateException("Cannot access polling endpoint: Server error");
        }

        ctx.close();
        this.handleCommands(response.content().toString(StandardCharsets.UTF_8));
        this.initiatePollRequest();
    }

    /**
     * Handles all incoming commands at once.
     */
    private void handleCommands(@Nonnull String messageContent) {
        JSONParser parser = new JSONParser();

        try {
            JSONArray commands = (JSONArray) parser.parse(messageContent);

            for (Object obj : commands) {
                JSONObject command = (JSONObject) obj;
                JSONArray commandTemplates = (JSONArray) command.get("commands");

                UUID identifier = UUID.fromString((String) command.get("uuid"));
                String name = (String) command.get("name");
                boolean requiresPlayer = command.containsKey("requiresPlayer") && (boolean) command.get("requiresPlayer");
                Player player = Bukkit.getPlayer(identifier);

                List<CommandTemplate> commandList = new ArrayList<>();

                for (Object cmdObj : commandTemplates) {
                    if (player != null) {
                        commandList.add(new CommandTemplate(player, (String) cmdObj));
                    } else {
                        commandList.add(new CommandTemplate(identifier, name, (String) cmdObj));
                    }
                }

                if (!requiresPlayer || player != null) {
                    commandList.forEach(CommandTemplate::execute);
                } else {
                    try (Connection connection = this.plugin.getDataSource().getConnection()) {
                        connection.setAutoCommit(false);

                        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO command_queue (template, profileId) VALUES (?, ?)")) {
                            for (CommandTemplate template : commandList) {
                                stmt.setString(1, template.getCommandTemplate());
                                stmt.setString(2, identifier.toString());
                                stmt.addBatch();
                            }

                            stmt.executeBatch();
                        }

                        connection.commit();
                    } catch (SQLException ex) {
                        this.plugin.getLogger().log(Level.SEVERE, "Could not store queued commands for player " + name + " (UUID " + identifier + "): " + ex.getMessage(), ex);
                    }
                }
            }
        } catch (ParseException | ClassCastException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Received an invalid response from the MinePay API: " + ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        this.initiatePollRequest();
    }

    /**
     * Initiates a new poll request.
     */
    private void initiatePollRequest() {
        Channel channel = this.bootstrap.connect("api.minepay.net", 443).syncUninterruptibly().channel();

        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/v1/bidi/longpoll");
        request.headers().set(HttpHeaders.Names.HOST, "api.minepay.net");
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        request.headers().set(HttpHeaders.Names.ACCEPT_CHARSET, "UTF-8");
        request.headers().set("X-ServerId", this.plugin.getConfiguration().getServerId());

        channel.writeAndFlush(request);
    }

    /**
     * Attempts to shut down the worker group gracefully.
     */
    public void shutdown() {
        this.workerGroup.shutdownGracefully().awaitUninterruptibly();
    }
}
