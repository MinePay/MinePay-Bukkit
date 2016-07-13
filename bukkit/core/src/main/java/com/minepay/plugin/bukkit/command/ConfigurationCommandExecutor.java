package com.minepay.plugin.bukkit.command;

import com.minepay.plugin.bukkit.MinePayPlugin;
import com.minepay.plugin.bukkit.telemetry.DataPoint;
import com.minepay.plugin.bukkit.telemetry.Submission;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.annotation.Nonnull;

/**
 * Provides a command which allows its users to configure the plugin without restarting the server.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class ConfigurationCommandExecutor implements CommandExecutor {
    private final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    private final MinePayPlugin plugin;

    public ConfigurationCommandExecutor(@Nonnull MinePayPlugin plugin) {
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

        String version = this.getClass().getPackage().getImplementationVersion();
        sender.sendMessage(StringUtils.center(ChatColor.GREEN + " MinePay " + (version != null ? "v" + version : "(Development Snapshot)") + " " + ChatColor.WHITE, 59, "-"));

        if (args.length != 0) {
            switch (args[0].toLowerCase()) {
                case "serverid":
                    if (args.length == 1 || args.length > 2) {
                        if (args.length > 2) {
                            sender.sendMessage(ChatColor.RED + "Too many arguments");
                            sender.sendMessage("");
                        }

                        sender.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.WHITE + label + " serverId <serverId>");
                        sender.sendMessage(ChatColor.GREEN + "Description: " + ChatColor.WHITE + "Registers the server with MinePay");
                        sender.sendMessage("");
                        return true;
                    }

                    // FIXME: serverIds should be verified before assuming that they work correctly

                    this.plugin.getConfiguration().setServerId(args[1]);
                    this.plugin.enableFunctionality();
                    sender.sendMessage("The server has been registered " + ChatColor.GREEN + "successfully");
                    sender.sendMessage("");
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
                                return true;
                            case "opt-out":
                                this.plugin.getConfiguration().setTelemetryEnabled(false);
                                this.plugin.saveConfiguration();
                                this.plugin.disableTelemetry();

                                sender.sendMessage("Telemetry has been " + ChatColor.RED + "disabled " + ChatColor.WHITE + "permanently");
                                sender.sendMessage("");
                                return true;
                            case "enable":
                                if (!this.plugin.getConfiguration().getServerId().isEmpty()) {
                                    this.plugin.enableTelemetry();
                                    sender.sendMessage("Telemetry has been " + ChatColor.GREEN + "enabled " + ChatColor.WHITE + "temporarily");
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Server must be registered in order to enable telemetry");
                                }
                                sender.sendMessage("");
                                return true;
                            case "disable":
                                if (!this.plugin.getConfiguration().getServerId().isEmpty()) {
                                    this.plugin.disableTelemetry();
                                    sender.sendMessage("Telemetry has been " + ChatColor.RED + "disabled " + ChatColor.WHITE + "temporarily");
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Server must be registered in order to enable telemetry");
                                }
                                sender.sendMessage("");
                                return true;
                            case "latest":
                                Submission submission = this.plugin.getLatestSubmission();

                                if (submission != null) {
                                    sender.sendMessage("The following telemetry report has been generated at " + ChatColor.GREEN + TIMESTAMP_FORMAT.format(submission.getGenerationTimestamp()) + ChatColor.WHITE + " and was submitted to the MinePay servers. If you wish to opt-out of this service, execute " + ChatColor.GREEN + "\"/" + label + " telemetry opt-out\"" + ChatColor.WHITE + " from your server console or the in-game chat. Please note, that disabling telemetry may interfere with some of the MinePay services.");
                                    sender.sendMessage("");

                                    for (DataPoint dataPoint : submission) {
                                        sender.sendMessage("--- " + ChatColor.GREEN + dataPoint.getName());

                                        final String type;
                                        final String value;

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

                                    sender.sendMessage(StringUtils.center(ChatColor.GRAY + " End of Report " + ChatColor.WHITE, 59, "-"));
                                    return true;
                                }

                                sender.sendMessage(ChatColor.RED + "No telemetry has been sent since the plugin was loaded");
                                sender.sendMessage("");
                                return true;
                            default:
                                sender.sendMessage(ChatColor.RED + "Unknown command");
                                sender.sendMessage("");
                        }
                    } else if (args.length > 2) {
                        sender.sendMessage(ChatColor.RED + "Too many arguments");
                        sender.sendMessage("");
                    }

                    sender.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.WHITE +  label + " telemetry <command>");
                    sender.sendMessage(ChatColor.GREEN + "Description: " + ChatColor.WHITE + "En- or Disables telemetry");
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GREEN + "Valid commands are:");
                    sender.sendMessage(ChatColor.GREEN + "    opt-in " + ChatColor.WHITE + "- Enables telemetry permanently");
                    sender.sendMessage(ChatColor.GREEN + "    opt-out " + ChatColor.WHITE + "- Disables telemetry permanently");
                    sender.sendMessage(ChatColor.GREEN + "    enable " + ChatColor.WHITE + "- Temporarily enables telemetry");
                    sender.sendMessage(ChatColor.GREEN + "    disable " + ChatColor.WHITE + "- Temporarily disables telemetry");
                    sender.sendMessage(ChatColor.GREEN + "    latest " + ChatColor.WHITE + " - Retrieves the latest data set");
                    sender.sendMessage("");
                    return true;
                default:
                    sender.sendMessage(ChatColor.RED + "Invalid Command");
                    sender.sendMessage("");
            }
        }

        if (!this.plugin.getConfiguration().getServerId().isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "Server ID: " + ChatColor.WHITE + this.plugin.getConfiguration().getServerId());
            sender.sendMessage(ChatColor.GREEN + "Average TPS: " + ChatColor.WHITE + this.plugin.getTickAverage());
            sender.sendMessage("Telemetry is " + (this.plugin.isTelemetryEnabled() ? ChatColor.GREEN + "enabled" + ChatColor.WHITE : ChatColor.RED + "disabled" + ChatColor.WHITE));
        } else {
            sender.sendMessage(ChatColor.RED + "This server has not been registered yet");
        }

        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.WHITE + label +" <command> [arguments]");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "Valid commands are:");
        sender.sendMessage(ChatColor.GREEN + "    serverId " + ChatColor.WHITE + " - Registers the server with MinePay");
        sender.sendMessage(ChatColor.GREEN + "    telemetry " + ChatColor.WHITE + " - En- or Disables telemetry");
        sender.sendMessage("");

        return true;
    }
}
