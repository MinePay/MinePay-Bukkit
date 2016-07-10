package net.minepay.plugin.bukkit.boilerplate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Provides a boilerplate for older Bukkit versions in order to work around changes to the API.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class OutdatedBukkitBoilerplate implements BukkitBoilerplate {

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        // A view would probably be a better way of handling this, however the reconstruction of
        // this array in Bukkit itself would break this concept majorly ... which is probably why
        // this method was deprecated in newer Bukkit versions
        return Arrays.asList(Bukkit.getOnlinePlayers());
    }
}
