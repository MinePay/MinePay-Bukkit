package com.minepay.plugin.bukkit.command;

import com.google.common.collect.ImmutableMap;

import com.minepay.plugin.bukkit.MinePayPlugin;
import com.minepay.plugin.bukkit.command.annotation.CommandHandler;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Provides a basic command executor which utilizes marker interfaces such as {@link
 * com.minepay.plugin.bukkit.command.annotation.CommandHandler} in order to simplify command handler
 * implementations.
 *
 * Note: This implementation will <strong>not</strong> register any commands. It relies entirely on
 * commands being registered already through the plugin's plugin.yml.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class SimpleCommandExecutor implements CommandExecutor {
    private final MinePayPlugin plugin;
    private final Map<String, CommandHandlerRegistration> handlerMap;

    public SimpleCommandExecutor(@Nonnull MinePayPlugin plugin) {
        this.plugin = plugin;
        ImmutableMap.Builder<String, CommandHandlerRegistration> builder = ImmutableMap.builder();

        for (Method method : this.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(CommandHandler.class)) {
                continue;
            }

            method.setAccessible(true);

            try {
                builder.put(method.getName().toLowerCase(), new CommandHandlerRegistration(method));
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Cannot access handler method \"" + method.getName() + "\": " + ex.getMessage(), ex);
            }
        }

        this.handlerMap = builder.build();
    }

    /**
     * Retrieves the parent plugin instance.
     *
     * @return a plugin instance.
     */
    @Nonnull
    protected MinePayPlugin getPlugin() {
        return this.plugin;
    }

    /**
     * Retrieves the command usage.
     *
     * @return a usage string.
     */
    @Nonnull
    protected String getUsage() {
        return "<command>";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        if (arguments.length != 0) {
            CommandHandlerRegistration handler = this.handlerMap.get(arguments[0]);

            if (handler != null) {
                String[] subArguments = new String[arguments.length - 1];

                if (subArguments.length != 0) {
                    System.arraycopy(arguments, 1, subArguments, 0, subArguments.length);

                    try {
                        handler.getHandler().invoke(this, sender, command, label, subArguments);
                    } catch (RuntimeException ex) {
                        throw ex;
                    } catch (Throwable ex) {
                        throw new RuntimeException("Could not pass command event to handler for sub-command \"" + arguments[0] + "\": " + ex.getMessage(), ex);
                    }
                } else {
                    this.printUsage(sender, "/" + label + " " + arguments[0] + " " + handler.configuration.usage(), "command." + command.getName().toLowerCase() + "." + arguments[0].toLowerCase() + ".description");
                    String[] usageArray = new String[(handler.configuration.subCommands().length * 2)];

                    for (int i = 0; i < usageArray.length; i += 2) {
                        usageArray[i] = handler.configuration.subCommands()[(i / 2)];
                        usageArray[(i + 1)] = "command." + command.getName().toLowerCase() + "." + arguments[0].toLowerCase() + "." + usageArray[i] + ".description";
                    }

                    this.printSubCommands(sender, usageArray);
                }

                return true;
            }

            this.printLocalized(sender, "command.unknown");
            sender.sendMessage("");
        }

        this.printUsage(sender, "/" + label + " " + this.getUsage(), "command." + command.getName().toLowerCase() + ".description");
        String[] usageArray = new String[(this.handlerMap.size() * 2)];
        int i = 0;

        for (Map.Entry<String, CommandHandlerRegistration> entry : this.handlerMap.entrySet()) {
            usageArray[i] = entry.getKey();
            usageArray[(i + 1)] = "command." + command.getName().toLowerCase() + "." + usageArray[i] + ".description";

            i += 2;
        }

        this.printSubCommands(sender, usageArray);
        return true;
    }

    /**
     * Prints a localized message to the console.
     *
     * @param sender    a sender.
     * @param key       a key.
     * @param arguments a set of arguments.
     */
    protected void printLocalized(@Nonnull CommandSender sender, @Nonnull String key, @Nonnull Object... arguments) {
        sender.sendMessage(this.plugin.getLocalizationManager().get(key, arguments));
    }

    /**
     * Prints the command usage.
     *
     * @param sender      a sender.
     * @param usage       a usage string.
     * @param description a command description.
     */
    protected void printUsage(@Nonnull CommandSender sender, @Nonnull String usage, @Nonnull String description) {
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
    protected void printSubCommands(@Nonnull CommandSender sender, @Nonnull String... elements) {
        if ((elements.length % 2) == 1) {
            throw new IllegalArgumentException("Cannot print command without description");
        }

        this.printLocalized(sender, "command.subcommands");
        for (int i = 0; i < elements.length; i += 2) {
            sender.sendMessage("    " + ChatColor.GREEN + elements[i] + ChatColor.WHITE + " - " + this.plugin.getLocalizationManager().get(elements[i + 1]));
        }
    }

    /**
     * Represents a command handler registration.
     */
    private class CommandHandlerRegistration {
        private final CommandHandler configuration;
        private final MethodHandle handler;

        public CommandHandlerRegistration(@Nonnull CommandHandler configuration, @Nonnull MethodHandle handler) {
            this.configuration = configuration;
            this.handler = handler;
        }

        public CommandHandlerRegistration(@Nonnull Method method) throws IllegalAccessException {
            this(method.getAnnotation(CommandHandler.class), MethodHandles.lookup().unreflect(method));
        }

        @Nonnull
        public CommandHandler getConfiguration() {
            return this.configuration;
        }

        @Nonnull
        public MethodHandle getHandler() {
            return this.handler;
        }
    }
}
