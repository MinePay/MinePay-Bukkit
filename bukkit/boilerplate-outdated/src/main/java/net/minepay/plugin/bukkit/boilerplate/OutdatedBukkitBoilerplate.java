package net.minepay.plugin.bukkit.boilerplate;

import org.bukkit.entity.Player;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class OutdatedBukkitBoilerplate implements BukkitBoilerplate {

    @Nonnull
    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return null;
    }
}
