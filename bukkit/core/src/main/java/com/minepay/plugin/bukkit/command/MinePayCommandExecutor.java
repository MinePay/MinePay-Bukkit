package com.minepay.plugin.bukkit.command;

import com.minepay.plugin.bukkit.MinePayPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

/**
 * Provides a command which allows its users to configure the plugin without restarting the server.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class MinePayCommandExecutor implements CommandExecutor {
    private final MinePayPlugin plugin;

    public MinePayCommandExecutor(@Nonnull MinePayPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission("minepay.administration")) {
            return false;
        }

        if (args.length != 0) {
            switch (args[0].toLowerCase()) {
                case "serverid":
                    if (args.length == 1 || args.length > 2) {
                        if (args.length > 2) {
                            sender.sendMessage(ChatColor.RED + "Too many arguments");
                            sender.sendMessage("");
                        }

                        sender.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.WHITE + "mp serverId <serverId>");
                        sender.sendMessage(ChatColor.GREEN + "Description: " + ChatColor.WHITE + "Registers the server with MinePay");
                        return true;
                    }

                    // FIXME: serverIds should be verified before assuming that they work correctly

                    this.plugin.getConfiguration().setServerId(args[1]);
                    return true;
                case "telemetry":
                    if (args.length == 2) {
                        switch (args[1].toLowerCase()) {
                            case "opt-in":
                                this.plugin.getConfiguration().setTelemetryEnabled(true);
                                this.plugin.saveConfiguration();

                                if (!this.plugin.getConfiguration().getServerId().isEmpty()) {
                                    this.plugin.enableTelemetry();
                                }

                                sender.sendMessage("Telemetry has been " + ChatColor.GREEN + "enabled " + ChatColor.WHITE + "permanently");
                                break;
                            case "opt-out":
                                this.plugin.getConfiguration().setTelemetryEnabled(false);
                                this.plugin.saveConfiguration();
                                this.plugin.disableTelemetry();

                                sender.sendMessage("Telemetry has been " + ChatColor.RED + "disabled " + ChatColor.WHITE + "permanently");
                                break;
                            case "enable":
                                if (!this.plugin.getConfiguration().getServerId().isEmpty()) {
                                    this.plugin.enableTelemetry();
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Server must be registered in order to enable telemetry");
                                }
                                break;
                            case "disable":
                                if (!this.plugin.getConfiguration().getServerId().isEmpty()) {
                                    this.plugin.disableTelemetry();
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Server must be registered in order to enable telemetry");
                                }
                                break;
                            default:
                                sender.sendMessage(ChatColor.RED + "Unknown command");
                                sender.sendMessage("");
                        }
                    } else if (args.length > 2) {
                        sender.sendMessage(ChatColor.RED + "Too many arguments");
                        sender.sendMessage("");
                    }

                    sender.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.WHITE + "mp telemetry <command>");
                    sender.sendMessage(ChatColor.GREEN + "Description: " + ChatColor.WHITE + "En- or Disables telemetry");
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GREEN + "Valid commands are:");
                    sender.sendMessage(ChatColor.GREEN + "    opt-in " + ChatColor.WHITE + "- Enables telemetry permanently");
                    sender.sendMessage(ChatColor.GREEN + "    opt-out " + ChatColor.WHITE + "- Disables telemetry permanently");
                    sender.sendMessage(ChatColor.GREEN + "    enable " + ChatColor.WHITE + "- Temporarily enables telemetry");
                    sender.sendMessage(ChatColor.GREEN + "    disable " + ChatColor.WHITE + "- Temporarily disables telemetry");
                    return true;
                default:
                    sender.sendMessage(ChatColor.RED + "Invalid Command");
                    sender.sendMessage("");
            }
        }

        String version = this.getClass().getPackage().getImplementationVersion();
        sender.sendMessage(ChatColor.GREEN + "MinePay Plugin v" + version);

        if (!this.plugin.getConfiguration().getServerId().isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Server ID: " + ChatColor.WHITE + this.plugin.getConfiguration().getServerId());
            sender.sendMessage(ChatColor.GREEN + "Average TPS: " + ChatColor.WHITE + this.plugin.getTickAverage());
            sender.sendMessage("Telemetry is " + (this.plugin.isTelemetryEnabled() ? ChatColor.GREEN + "enabled" + ChatColor.WHITE : ChatColor.RED + "disabled" + ChatColor.WHITE));
        } else {
            sender.sendMessage(ChatColor.RED + "This server has not been registered yet");
        }

        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.WHITE + "mp <command> [arguments]");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "Valid commands are:");
        sender.sendMessage(ChatColor.GREEN + "    serverId " + ChatColor.WHITE + " - Registers the server with MinePay using its serverId");
        sender.sendMessage(ChatColor.GREEN + "    telemetry " + ChatColor.WHITE + " - En- or Disables telemetry");
        sender.sendMessage("");
        sender.sendMessage("Please contact MinePay support if you are experiencing any issues with this plugin");

        return true;
    }
}
