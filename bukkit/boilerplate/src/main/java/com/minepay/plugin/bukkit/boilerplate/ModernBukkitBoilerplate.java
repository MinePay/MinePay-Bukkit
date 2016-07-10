package com.minepay.plugin.bukkit.boilerplate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Provides a boilerplate for modern implementations which are expected to respect the API contract.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class ModernBukkitBoilerplate implements BukkitBoilerplate {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }
}
