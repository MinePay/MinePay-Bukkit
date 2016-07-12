package com.minepay.plugin.bukkit.boilerplate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Provides a boilerplate around the Bukkit API in order to mitigate backwards incompatible
 * changes.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public interface BukkitBoilerplate {
    Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(.*)$");

    /**
     * Retrieves a Bukkit boilerplate instance which is designed to wrap the current server API
     * version.
     *
     * @return a boilerplate implementation.
     */
    @Nonnull
    static BukkitBoilerplate getInstance() {
        Matcher matcher = VERSION_PATTERN.matcher(Bukkit.getVersion());

        if (matcher.matches()) {
            return new ModernBukkitBoilerplate();
        }

        try {
            int major = Integer.parseUnsignedInt(matcher.group(1));
            int minor = Integer.parseUnsignedInt(matcher.group(2));

            if (major <= 1 && minor <= 7) {
                return Class.forName("com.minepay.plugin.bukkit.boilerplate").asSubclass(BukkitBoilerplate.class).newInstance();
            }
        } catch (NumberFormatException ignore) {
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Could not load compatibility layer: " + ex.getMessage(), ex);
        }

        return new ModernBukkitBoilerplate();
    }

    /**
     * Retrieves a collection of players which are currently connected to the server.
     *
     * @return a collection of players.
     */
    @Nonnull
    Collection<? extends Player> getOnlinePlayers();
}
