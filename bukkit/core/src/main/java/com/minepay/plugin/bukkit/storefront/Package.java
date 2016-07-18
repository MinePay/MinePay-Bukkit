package com.minepay.plugin.bukkit.storefront;

import com.minepay.plugin.bukkit.LocalizationManager;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class Package {
    private final long id;
    private final String name;
    private final String description;
    private final Material guiItem;
    private final BigDecimal price;

    public Package(long id, @Nonnull String name, @Nonnull String description, @Nonnull Material guiItem, @Nonnull BigDecimal price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.guiItem = guiItem;
        this.price = price;
    }

    public Package(@Nonnull JSONObject object) {
        this.id = (long) object.get("id");
        this.name = (String) object.get("name");
        this.description = (String) object.get("description");
        this.guiItem = Material.valueOf(((String) object.get("guiItem")).toUpperCase());
        this.price = BigDecimal.valueOf((float) object.get("price")); // TODO: Prices should not be passed as floats!
    }

    public long getId() {
        return this.id;
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nonnull
    public String getDescription() {
        return this.description;
    }

    @Nonnull
    public Material getGuiItem() {
        return this.guiItem;
    }

    @Nonnull
    public BigDecimal getPrice() {
        return this.price;
    }

    /**
     * Retrieves an item stack which will be displayed as a menu icon in the buy menu.
     *
     * @param localizationManager a localization manager.
     * @return an item stack.
     */
    @Nonnull
    public ItemStack getIcon(@Nonnull LocalizationManager localizationManager) {
        ItemStack icon = new ItemStack(this.guiItem, 1);

        // Bukkit stupidity
        ItemMeta meta = icon.getItemMeta();
        {
            meta.setDisplayName(this.name);
            meta.setLore(Arrays.asList(
                    this.description,
                    "",
                    localizationManager.get("buy.package.price", this.price)
            ));
        }
        icon.setItemMeta(meta);
        return icon;
    }
}
