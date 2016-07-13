package com.minepay.plugin.bukkit.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * Represents a stored command template for a certain player.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class CommandTemplate {
    private final UUID identifier;
    private final String name;
    private final String template;

    public CommandTemplate(@Nonnull UUID identifier, @Nonnull String name, @Nonnull String template) {
        this.identifier = identifier;
        this.name = name;
        this.template = template;
    }

    public CommandTemplate(@Nonnull Player player, @Nonnull String template) {
        this(player.getUniqueId(), player.getName(), template);
    }

    /**
     * Executes the command according to the specifications.
     */
    public void execute() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.getCommand());
    }

    /**
     * Retrieves the full console command.
     *
     * @return a command.
     */
    @Nonnull
    public String getCommand() {
        return this.template.replace("{uuid}", this.identifier.toString()).replace("{name}", this.name);
    }

    /**
     * Retrieves the command template.
     *
     * @return a template.
     */
    @Nonnull
    public String getCommandTemplate() {
        return this.template;
    }
}
