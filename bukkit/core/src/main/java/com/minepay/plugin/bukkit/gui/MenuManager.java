package com.minepay.plugin.bukkit.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Handles the lifecycle of container based menus.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class MenuManager implements Listener {
    private final Map<Inventory, Menu> menuMap = new HashMap<>();

    /**
     * Creates a managed menu utilizing the specified size.
     *
     * <strong>Note:</strong> The inventory size must be a multiple of 9.
     *
     * @param player a player.
     * @param size   a size.
     * @return a menu.
     */
    @Nonnull
    public Menu create(@Nonnull Player player, @Nonnegative int size) {
        return this.create(player, Bukkit.createInventory(player, size));
    }

    /**
     * Creates a managed menu utilizing the specified size and title.
     *
     * <strong>Note:</strong> The inventory size must be a multiple of 9.
     *
     * @param player a player.
     * @param title  a title.
     * @param size   a size.
     * @return a menu.
     */
    @Nonnull
    public Menu create(@Nonnull Player player, @Nonnull String title, @Nonnegative int size) {
        return this.create(player, Bukkit.createInventory(player, size, title));
    }

    /**
     * Creates a managed menu utilizing an existing menu.
     *
     * @param player    a player.
     * @param inventory an inventory.
     * @return a menu.
     */
    @Nonnull
    public Menu create(@Nonnull Player player, @Nonnull Inventory inventory) {
        Menu menu = new Menu(this, player, inventory);
        this.menuMap.put(inventory, menu);
        return menu;
    }

    /**
     * Destroys a menu item completely.
     *
     * @param inventory an inventory.
     */
    void destroy(@Nonnull Inventory inventory) {
        this.menuMap.remove(inventory);
    }

    /**
     * Handles inventory click events, cancels and passes them on to their respective menus.
     *
     * @param event an event.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(@Nonnull InventoryClickEvent event) {
        Menu menu = this.menuMap.get(event.getInventory());

        if (menu != null) {
            event.setCancelled(true);
            menu.onClick(event.getSlot(), event.getAction());
        }
    }

    /**
     * Handles inventory close events, passes them on to their respective menus and frees the memory
     * associated with said menu.
     *
     * @param event an event.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClose(@Nonnull InventoryCloseEvent event) {
        Menu menu = this.menuMap.get(event.getInventory());

        if (menu != null) {
            if (!menu.onClose()) {
                event.getPlayer().openInventory(event.getInventory());
            } else {
                this.menuMap.remove(event.getInventory());
            }
        }
    }

    /**
     * Cancels drag interactions in menus.
     *
     * @param event an event.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(@Nonnull InventoryDragEvent event) {
        if (this.menuMap.containsKey(event.getInventory())) {
            event.setCancelled(true);
        }
    }
}
