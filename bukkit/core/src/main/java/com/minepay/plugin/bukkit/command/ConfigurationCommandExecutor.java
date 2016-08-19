package com.minepay.plugin.bukkit.command;

import com.minepay.plugin.bukkit.MinePayPlugin;
import com.minepay.plugin.bukkit.command.annotation.CommandHandler;
import com.minepay.plugin.bukkit.telemetry.DataPoint;
import com.minepay.plugin.bukkit.telemetry.Submission;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * Provides a command which allows its users to configure the plugin without restarting the server.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class ConfigurationCommandExecutor extends SimpleCommandExecutor {
    private final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    private final String METADATA_ENDPOINT_URL = "https://api.minepay.net/v1/metadata";

    public ConfigurationCommandExecutor(@Nonnull MinePayPlugin plugin) {
        super(plugin);
    }

    @SuppressWarnings("deprecation")
    @CommandHandler(usage = "<serverId|clear>", subCommands = "clear")
    public void serverId(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        if (arguments.length != 1) {
            this.printLocalized(sender, "command.arguments.many");
            return;
        }

        if ("clear".equals(arguments[0])) {
            this.getPlugin().disableFunctionality();
            this.getPlugin().getConfiguration().setServerId("");
            this.getPlugin().saveConfiguration();

            this.printLocalized(sender, "configuration.serverId.clear");
            return;
        }

        Bukkit.getScheduler().scheduleAsyncDelayedTask(this.getPlugin(), () -> {
            try {
                final JSONParser parser = new JSONParser();
                HttpURLConnection connection = (HttpURLConnection) new URL(METADATA_ENDPOINT_URL).openConnection();

                final JSONObject object;
                final int responseCode = connection.getResponseCode();

                if (responseCode != 200) {
                    object = null;
                } else {
                    try (InputStream inputStream = connection.getInputStream()) {
                        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                            object = (JSONObject) parser.parse(reader);
                        }
                    }
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), () -> {
                    if (responseCode == 401 || responseCode == 403) {
                        sender.sendMessage(this.getPlugin().getLocalizationManager().get("command.minepay.serverid.invalid"));
                    } else if (responseCode >= 500) {
                        sender.sendMessage(this.getPlugin().getLocalizationManager().get("error.remote.unavailable"));
                    } else if (responseCode != 200) {
                        sender.sendMessage(this.getPlugin().getLocalizationManager().get("error.remote.unknown"));
                    } else {
                        JSONArray array = (JSONArray) object.get("stores");

                        this.getPlugin().getConfiguration().setServerId(arguments[0]);
                        this.getPlugin().getConfiguration().setStoreName((String) array.get(0));

                        this.getPlugin().enableFunctionality();
                        this.getPlugin().saveConfiguration();

                        this.printLocalized(sender, "configuration.serverId.success");
                    }
                });
            } catch (IOException | ParseException ex) {
                sender.sendMessage(this.getPlugin().getLocalizationManager().get("error.remote.unknown"));
                this.getPlugin().getLogger().log(Level.SEVERE, "Could not read and parse Minepay response: " + ex.getMessage(), ex);
            }
        });
    }

    @CommandHandler(usage = "<opt-in|opt-out|enable|disable|latest>", subCommands = {"opt-in", "opt-out", "enable", "disable", "latest"})
    public void telemetry(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        if (arguments.length != 1) {
            this.printLocalized(sender, "commands.arguments.many");
            return;
        }

        switch (arguments[0].toLowerCase()) {
            case "opt-in":
                this.getPlugin().getConfiguration().setTelemetryEnabled(true);
                this.getPlugin().saveConfiguration();

                if (!this.getPlugin().getConfiguration().getServerId().isEmpty()) {
                    this.getPlugin().enableTelemetry();
                }

                this.printLocalized(sender, "configuration.telemetry.opt-in");
                break;
            case "opt-out":
                this.getPlugin().getConfiguration().setTelemetryEnabled(false);
                this.getPlugin().saveConfiguration();
                this.getPlugin().disableTelemetry();

                this.printLocalized(sender, "configuration.telemetry.opt-out");
                break;
            case "enable":
                if (!this.getPlugin().getConfiguration().getServerId().isEmpty()) {
                    this.getPlugin().enableTelemetry();
                    this.printLocalized(sender, "configuration.telemetry.enable");
                } else {
                    this.printLocalized(sender, "warning.configuration.unregistered");
                }
                break;
            case "disable":
                if (!this.getPlugin().getConfiguration().getServerId().isEmpty()) {
                    this.getPlugin().disableTelemetry();
                    this.printLocalized(sender, "configuration.telemetry.disable");
                } else {
                    this.printLocalized(sender, "warning.configuration.unregistered");
                }
                break;
            case "latest":
                Submission submission = this.getPlugin().getLatestSubmission();

                if (submission != null) {
                    this.printLocalized(sender, "configuration.telemetry.latest.header", TIMESTAMP_FORMAT.format(submission.getGenerationTimestamp()), label);
                    sender.sendMessage("");

                    for (DataPoint dataPoint : submission) {
                        sender.sendMessage("--- " + ChatColor.GREEN + dataPoint.getName());

                        final String type;
                        final String value;

                        // TODO: Localization
                        if (dataPoint instanceof DataPoint.FloatDataPoint) {
                            type = "Float";
                            value = Float.toString(((DataPoint.FloatDataPoint) dataPoint).getValue());
                        } else if (dataPoint instanceof DataPoint.IntegerDataPoint) {
                            type = "Integer";
                            value = Integer.toString(((DataPoint.IntegerDataPoint) dataPoint).getValue());
                        } else if (dataPoint instanceof DataPoint.LongDataPoint) {
                            type = "Long";
                            value = Long.toString(((DataPoint.LongDataPoint) dataPoint).getValue());
                        } else {
                            type = "Unknown";
                            value = "";
                        }

                        sender.sendMessage(ChatColor.GREEN + "Type: " + ChatColor.WHITE + type);
                        sender.sendMessage(ChatColor.GREEN + "Value: " + ChatColor.WHITE + value);
                        sender.sendMessage("");
                    }

                    sender.sendMessage(StringUtils.center(this.getPlugin().getLocalizationManager().get("configuration.telemetry.latest.end"), 59, "-"));
                } else {
                    this.printLocalized(sender, "configuration.telemetry.latest.no-data");
                }
                break;
            default:
                this.printLocalized(sender, "command.unknown");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        if (!sender.hasPermission("minepay.administration")) {
            return false;
        }

        String version = this.getClass().getPackage().getImplementationVersion();
        sender.sendMessage(StringUtils.center(ChatColor.GREEN + " MinePay " + (version != null ? "v" + version : "(Development Snapshot)") + " " + ChatColor.WHITE, 59, "-"));

        if (arguments.length == 0 && !this.getPlugin().getConfiguration().getServerId().isEmpty()) {
            this.printLocalized(sender, "command.minepay.state.serverId", StringUtils.overlay(this.getPlugin().getConfiguration().getServerId(), "******", (this.getPlugin().getConfiguration().getServerId().length() - 7), this.getPlugin().getConfiguration().getServerId().length()));
            this.printLocalized(sender, "command.minepay.state.tps", this.getPlugin().getTickAverage());
            this.printLocalized(sender, "command.minepay.state.telemetry." + (this.getPlugin().isTelemetryEnabled() ? "enabled" : "disabled"));
            sender.sendMessage("");
        }

        return super.onCommand(sender, command, label, arguments);
    }
}
