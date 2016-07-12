package com.minepay.plugin.bukkit.boilerplate.v1_10_R1;

import net.minecraft.server.v1_10_R1.MinecraftServer;
import com.minepay.plugin.bukkit.boilerplate.CraftBukkitBoilerplate;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Provides a CraftBukkit boilerplate which is capable of wrapping calls to NMS for a certain server
 * version.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class CraftBukkitBoilerplateImpl implements CraftBukkitBoilerplate {
    private final MethodHandle handle;

    public CraftBukkitBoilerplateImpl() throws NoSuchFieldException, IllegalAccessException {
        this.handle = MethodHandles.publicLookup().findGetter(MinecraftServer.class, "tick", Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTickCount() {
        try {
            return (int) this.handle.invoke(((CraftServer) Bukkit.getServer()).getHandle());
        } catch (Throwable ex) {
            throw new RuntimeException("Could not access NMS tick count: " + ex.getMessage(), ex);
        }
    }
}
