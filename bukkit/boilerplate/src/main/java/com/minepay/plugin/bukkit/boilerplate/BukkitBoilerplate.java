package com.minepay.plugin.bukkit.boilerplate;

import org.bukkit.entity.Player;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Provides a boilerplate around the Bukkit API in order to mitigate backwards incompatible
 * changes.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public interface BukkitBoilerplate {

    /**
     * Retrieves a collection of players which are currently connected to the server.
     *
     * @return a collection of players.
     */
    @Nonnull
    Collection<? extends Player> getOnlinePlayers();
}
