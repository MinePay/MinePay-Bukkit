package com.minepay.plugin.bukkit.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Signed;

/**
 * Represents an active menu which is displayed on a player's screen in form of a container (such as
 * a chest).
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class Menu {
    private final MenuManager manager;
    private final Player player;
    private final Inventory inventory;
    private final MenuItem[] items;

    Menu(@Nonnull MenuManager manager, @Nonnull Player player, @Nonnull Inventory inventory) {
        this.manager = manager;
        this.player = player;
        this.inventory = inventory;

        this.items = new MenuItem[inventory.getSize()];
    }

    /**
     * Handles click events on certain menu items.
     *
     * @param slot   a slot index.
     * @param action the action type.
     */
    public void onClick(@Nonnegative int slot, @Nonnull InventoryAction action) {
        if (slot >= this.items.length) {
            throw new IllegalStateException("Player attempted to access slot out of menu bounds: " + slot + " out of " + this.items.length + " available slots");
        }

        MenuItem item = this.items[slot];

        if (item != null) {
            item.onClick(action);
        }
    }

    /**
     * Handles and verifies the closing of a menu.
     *
     * @return true if closing the menu is allowed, false otherwise.
     */
    public boolean onClose() {
        return true;
    }

    /**
     * Adds a new item to the menu.
     *
     * @param slot a slot number.
     * @param item an item.
     */
    public void add(@Nonnegative int slot, @Nonnull MenuItem item) {
        this.items[slot] = item;
        this.inventory.setItem(slot, new ItemStack(item.getMaterial(), 1));
    }

    /**
     * Adds a new item to the menu.
     *
     * @param slot     a slot number.
     * @param material a material.
     * @param handler  a handler.
     */
    public MenuItem add(@Nonnegative int slot, @Nonnull Material material, @Nonnull BiConsumer<MenuItem, InventoryAction> handler) {
        MenuItem item = new MenuItem(material) {
            @Override
            public void onClick(@Nonnull InventoryAction action) {
                handler.accept(this, action);
            }
        };

        this.add(slot, item);
        return item;
    }

    /**
     * Clears the entire menu.
     */
    public void clear() {
        for (int i = 0; i < this.items.length; i++) {
            this.items[i] = null;
        }

        this.inventory.clear();
    }

    /**
     * Clears a certain slot.
     *
     * @param slot a slot.
     */
    public void clear(@Nonnegative int slot) {
        this.items[slot] = null;
        this.inventory.setItem(slot, null);
    }

    /**
     * Closes the menu.
     */
    public void close() {
        this.manager.destroy(this.inventory);

        if (this.player.getInventory() == this.inventory) {
            this.player.closeInventory();
        }
    }

    /**
     * Removes a certain item from the menu.
     *
     * @param item an item.
     */
    public void remove(@Nonnull MenuItem item) {
        int index = this.indexOf(item);

        if (index != -1) {
            this.clear(index);
        }
    }

    /**
     * Retrieves the slot index a menu item is currently on.
     *
     * @param item an item.
     * @return an index or, if no such item was found, -1.
     */
    @Signed
    public int indexOf(@Nonnull MenuItem item) {
        for (int i = 0; i < this.items.length; i++) {
            if (this.items[i] == item) {
                return i;
            }
        }

        return -1;
    }
}
