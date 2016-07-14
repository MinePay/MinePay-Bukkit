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
     * Prints a localized message to the console.
     *
     * @param sender    a sender.
     * @param key       a key.
     * @param arguments a set of arguments.
     */
    private void printLocalized(@Nonnull CommandSender sender, @Nonnull String key, @Nonnull Object... arguments) {
        sender.sendMessage(this.plugin.getLocalizationManager().get(key, arguments));
    }

    /**
     * Prints the command usage.
     *
     * @param sender      a sender.
     * @param usage       a usage string.
     * @param description a command description.
     */
    private void printUsage(@Nonnull CommandSender sender, @Nonnull String usage, @Nonnull String description) {
        sender.sendMessage(this.plugin.getLocalizationManager().get("command.usage", usage));
        sender.sendMessage(this.plugin.getLocalizationManager().get("command.description", this.plugin.getLocalizationManager().get(description)));
        sender.sendMessage("");
    }

    /**
     * Prints a list of valid sub commands.
     *
     * @param sender   a command sender.
     * @param elements a set of commands and their descriptions.
     */
    private void printSubCommands(@Nonnull CommandSender sender, @Nonnull String... elements) {
        if ((elements.length % 2) == 1) {
            throw new IllegalArgumentException("Cannot print command without description");
        }

        this.printLocalized(sender, "command.subcommands");
        for (int i = 0; i < elements.length; i += 2) {
            sender.sendMessage("    " + ChatColor.GREEN + elements[i] + ChatColor.WHITE + " - " + this.plugin.getLocalizationManager().get(elements[i + 1]));
        }
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
                            this.printLocalized(sender, "command.arguments.many");
                            return true;
                        }

                        this.printUsage(sender, "/" + label + " serverId <serverId|clear>", "command.mp.serverId.description");
                        return true;
                    }

                    if ("clear".equals(args[1])) {
                        this.plugin.disableFunctionality();
                        this.plugin.getConfiguration().setServerId("");
                        this.plugin.saveConfiguration();

                        this.printLocalized(sender, "configuration.serverId.clear");
                        return true;
                    }

                    // FIXME: serverIds should be verified before assuming that they work correctly

                    this.plugin.getConfiguration().setServerId(args[1]);
                    this.plugin.enableFunctionality();
                    this.plugin.saveConfiguration();

                    this.printLocalized(sender, "configuration.serverId.success");
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

                                this.printLocalized(sender, "configuration.telemetry.opt-in");
                                return true;
                            case "opt-out":
                                this.plugin.getConfiguration().setTelemetryEnabled(false);
                                this.plugin.saveConfiguration();
                                this.plugin.disableTelemetry();

                                this.printLocalized(sender, "configuration.telemetry.opt-out");
                                return true;
                            case "enable":
                                if (!this.plugin.getConfiguration().getServerId().isEmpty()) {
                                    this.plugin.enableTelemetry();
                                    this.printLocalized(sender, "configuration.telemetry.enable");
                                } else {
                                    this.printLocalized(sender, "warning.configuration.unregistered");
                                }
                                return true;
                            case "disable":
                                if (!this.plugin.getConfiguration().getServerId().isEmpty()) {
                                    this.plugin.disableTelemetry();
                                    this.printLocalized(sender, "configuration.telemetry.disable");
                                } else {
                                    this.printLocalized(sender, "warning.configuration.unregistered");
                                }
                                return true;
                            case "latest":
                                Submission submission = this.plugin.getLatestSubmission();

                                if (submission != null) {
                                    this.printLocalized(sender, "configuration.telemetry.latest.header", TIMESTAMP_FORMAT.format(submission.getGenerationTimestamp()));
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

                                    sender.sendMessage(StringUtils.center(this.plugin.getLocalizationManager().get("configuration.telemetry.latest.end"), 59, "-"));
                                    return true;
                                }

                                this.printLocalized(sender, "configuration.telemetry.latest.no-data");
                                return true;
                            default:
                                this.printLocalized(sender, "command.unknown");
                        }
                    } else if (args.length > 2) {
                        this.printLocalized(sender, "command.arguments.many");
                    }

                    this.printUsage(sender, "/" + label + " telemetry <command>", "command.mp.telemetry.description");
                    this.printSubCommands(
                            sender,
                            "opt-in", "command.mp.telemetry.opt-in.description",
                            "opt-out", "command.mp.telemetry.opt-out.description",
                            "enable", "command.mp.telemetry.enable.description",
                            "disable", "command.mp.telemetry.disable.description",
                            "latest", "command.mp.telemetry.latest.description"
                    );
                    return true;
                default:
                    this.printLocalized(sender, "command.unknown");
            }
        }

        if (!this.plugin.getConfiguration().getServerId().isEmpty()) {
            this.printLocalized(sender, "command.mp.state.serverId", this.plugin.getConfiguration().getServerId());
            this.printLocalized(sender, "command.mp.state.tps", this.plugin.getTickAverage());
            this.printLocalized(sender, "command.mp.state.telemetry." + (this.plugin.isTelemetryEnabled() ? "enabled" : "disabled"));
            sender.sendMessage("");
        }

        this.printUsage(sender, "/" + label, "command.mp.description");
        this.printSubCommands(
                sender,
                "serverId", "command.mp.serverId.description",
                "telemetry", "command.mp.telemetry.description"
        );
        return true;
    }
}
