package com.minepay.plugin.bukkit.storefront;

import com.minepay.plugin.bukkit.LocalizationManager;
import com.minepay.plugin.bukkit.gui.Menu;
import com.minepay.plugin.bukkit.gui.MenuManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class Cart extends Menu {
    private final LocalizationManager localizationManager;
    private final UUID cartId;
    private final List<Category> categories;
    private boolean selectedItem;
    private Category category;

    public Cart(@Nonnull MenuManager manager, @Nonnull LocalizationManager localizationManager, @Nonnull Player player, @Nonnull UUID cartId, @Nonnull List<Category> categories) {
        super(manager, player, Bukkit.createInventory(player, (categories.size() / 9) + 1, localizationManager.get("buy.title")));
        this.localizationManager = localizationManager;
        this.cartId = cartId;
        this.categories = categories;

        this.buildInterface();
    }

    /**
     * (Re-)builds the entire interface.
     */
    private void buildInterface() {
        this.clear();
        {
            if (this.category == null) {
                int i = 0;

                for (Category category : this.categories) {
                    this.add(i, category.getIcon(this.localizationManager), (m, a) -> this.selectCategory(category));

                    ++i;
                }
            } else {
                int i = 0;

                for (Package pkg : this.category.getPackages()) {
                    this.add(i, pkg.getIcon(localizationManager), (m, a) -> this.selectPackage(pkg));

                    ++i;
                }
            }
        }
        this.populateMenuButtons();
    }

    /**
     * Confirms the submission of a cart.
     */
    private void confirm() {
        // TODO: Submit to server & Notify user
        this.close();
    }

    /**
     * Populates the menu with a consistent set of buttons.
     */
    private void populateMenuButtons() {
        if (this.category != null) {
            ItemStack back = new ItemStack(Material.INK_SACK, 1, (short) 1);
            {
                ItemMeta meta = back.getItemMeta();
                meta.setDisplayName(this.localizationManager.get("buy.back"));
                back.setItemMeta(meta);
            }

            this.add(this.getSize() - 9, back, (m, a) -> {
                if (a == InventoryAction.PICKUP_ALL) {
                    this.category = null;
                    this.buildInterface();
                }
            });
        }

        if (this.selectedItem) {
            ItemStack confirm = new ItemStack(Material.INK_SACK, 1, (short) 2);
            {
                ItemMeta meta = confirm.getItemMeta();
                meta.setDisplayName(this.localizationManager.get("buy.confirm"));
                confirm.setItemMeta(meta);
            }

            this.add(this.getSize() - 1, confirm, (m, a) -> {
                if (a == InventoryAction.PICKUP_ALL) {
                    this.confirm();
                }
            });
        }
    }

    /**
     * Selects a category.
     *
     * @param category a category.
     */
    private void selectCategory(@Nonnull Category category) {
        this.category = category;
        this.buildInterface();
    }

    /**
     * Selects a package.
     *
     * @param pkg a package.
     */
    private void selectPackage(@Nonnull Package pkg) {
        this.selectedItem = true;

        // TODO: Add to cart
        // FIXME: Remove from item from selection if single purchase only?
    }
}
