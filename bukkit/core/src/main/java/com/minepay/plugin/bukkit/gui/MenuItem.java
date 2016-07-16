package com.minepay.plugin.bukkit.gui;

import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Represents a single menu item (represented by an item) which can be left or right clicked by a
 * player.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public abstract class MenuItem {
    private final ItemStack icon;

    public MenuItem(@Nonnull ItemStack icon) {
        this.icon = icon;
    }

    /**
     * Retrieves the item stack which represents this menu item's icon.
     *
     * @return an item stack.
     */
    public ItemStack getIcon() {
        return this.icon;
    }

    /**
     * Handles the invocation of this menu item.
     *
     * @param action an action type.
     */
    public void onClick(@Nonnull InventoryAction action) {
    }
}
