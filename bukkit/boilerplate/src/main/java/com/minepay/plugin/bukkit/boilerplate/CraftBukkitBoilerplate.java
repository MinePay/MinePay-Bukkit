package com.minepay.plugin.bukkit.boilerplate;

import org.bukkit.Bukkit;

import java.util.Optional;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Provides direct access to NMS in order to simplify or otherwise optimize processes.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public interface CraftBukkitBoilerplate {

    /**
     * Retrieves a boilerplate instance depending on the reported server version.
     *
     * @return a boilerplate implementation or, if no matching implementation could be located, an
     * empty optional.
     */
    @Nonnull
    static Optional<CraftBukkitBoilerplate> getInstance() {
        String serverVersion = Bukkit.getServer().getClass().getPackage().getName().replaceAll("^.*\\.v(\\d+)_(\\d+)_R(\\d+)\\..*$", "v$1_$2_R$3");

        try {
            Class<?> clazz = Class.forName("com.minepay.plugin.bukkit.boilerplate." + serverVersion + ".CraftBukkitBoilerplateImpl");
            return Optional.of(clazz.asSubclass(CraftBukkitBoilerplate.class).newInstance());
        } catch (ClassNotFoundException ignore) {
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException("Could not instantiate CraftBukkit support implementation from com.minepay.plugin.bukkit.boilerplate." + serverVersion + ".CraftBukkitBoilerplateImpl: " + ex.getMessage(), ex);
        }

        return Optional.empty();
    }

    /**
     * Retrieves the overall amount of ticks processed by the server since startup.
     *
     * @return an amount of ticks.
     */
    @Nonnegative
    int getTickCount();
}
