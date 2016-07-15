package com.minepay.plugin.bukkit.gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;

import javax.annotation.Nonnull;

/**
 * Represents a single menu item (represented by an item) which can be left or right clicked by a
 * player.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class MenuItem {
    private final Material material;

    public MenuItem(@Nonnull Material material) {
        this.material = material;
    }

    @Nonnull
    public Material getMaterial() {
        return this.material;
    }

    /**
     * Handles the invocation of this menu item.
     *
     * @param action an action type.
     */
    public void onClick(@Nonnull InventoryAction action) {
    }
}
