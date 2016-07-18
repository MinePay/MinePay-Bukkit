package com.minepay.plugin.bukkit.storefront;

import com.google.common.collect.ImmutableList;

import com.minepay.plugin.bukkit.LocalizationManager;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Represents a category in a server store
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class Category {
    private final long id;
    private final String name;
    private final String description;
    private final Material guiItem;
    private final List<Package> packages;

    public Category(long id, @Nonnull String name, @Nonnull String description, @Nonnull Material guiItem, @Nonnull List<Package> packages) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.guiItem = guiItem;
        this.packages = packages;
    }

    public Category(@Nonnull JSONObject object) {
        this.id = (long) object.get("id");
        this.name = (String) object.get("name");
        this.description = (String) object.get("description");
        this.guiItem = Material.valueOf(((String) object.get("guiItem")).toUpperCase());

        ImmutableList.Builder<Package> packages = ImmutableList.builder();
        {
            JSONArray array = (JSONArray) object.get("packages");
            for (Object obj : array) {
                packages.add(new Package((JSONObject) obj));
            }
        }
        this.packages = packages.build();
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
    public List<Package> getPackages() {
        return this.packages;
    }

    /**
     * Retrieves the menu item for this category.
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
                    localizationManager.get("buy.category.content", this.packages.size())
            ));
        }
        icon.setItemMeta(meta);
        return icon;
    }
}
