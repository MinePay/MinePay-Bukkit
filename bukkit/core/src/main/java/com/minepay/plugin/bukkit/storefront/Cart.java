package com.minepay.plugin.bukkit.storefront;

import com.minepay.plugin.bukkit.MinePayPlugin;
import com.minepay.plugin.bukkit.gui.Menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * Represents a cart which has been introduced to the Minepay servers for a user.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class Cart extends Menu {
    private static final String CART_ADD_ENDPOINT_URL = "https://api.minepay.net/v1/store/%s/cart/%s";
    private static final String CART_INFORMATION_ENDPOINT_URL = "https://api.minepay.net/v1/store/%s/cart/%s";

    private final MinePayPlugin plugin;
    private final UUID cartId;
    private final List<Category> categories;
    private boolean selectedItem;
    private Category category;

    public Cart(@Nonnull MinePayPlugin plugin, @Nonnull Player player, @Nonnull UUID cartId, @Nonnull List<Category> categories) {
        super(plugin.getMenuManager(), player, Bukkit.createInventory(player, (categories.size() / 9) + 1, plugin.getLocalizationManager().get("command.buy.title")));
        this.plugin = plugin;
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
                    this.add(i, category.getIcon(this.plugin.getLocalizationManager()), (m, a) -> this.selectCategory(category));

                    ++i;
                }
            } else {
                int i = 0;

                for (Package pkg : this.category.getPackages()) {
                    this.add(i, pkg.getIcon(this.plugin.getLocalizationManager()), (m, a) -> this.selectPackage(pkg));

                    ++i;
                }
            }
        }
        this.populateMenuButtons();
    }

    /**
     * Confirms the submission of a cart.
     */
    @SuppressWarnings("deprecation")
    private void confirm() {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(this.plugin, () -> {
            final JSONParser parser = new JSONParser();

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(String.format(CART_INFORMATION_ENDPOINT_URL, this.plugin.getConfiguration().getStoreName(), this.cartId)).openConnection();

                try (InputStream inputStream = connection.getInputStream()) {
                    try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                        final JSONObject object = (JSONObject) parser.parse(reader);
                        final CartResponse.Information information = new CartResponse.Information(object);

                        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                            this.close();
                            this.plugin.getCartManager().destroy(this.getPlayer());

                            this.getPlayer().sendMessage(this.plugin.getLocalizationManager().get("command.buy.confirm.response", information.getUrl()));
                        });
                    }
                }
            } catch (IOException | ParseException ex) {
                // TODO: Proper error handling
                this.getPlayer().sendMessage(this.plugin.getLocalizationManager().get("command.buy.failure"));
                ex.printStackTrace();
            }
        });
    }

    /**
     * Populates the menu with a consistent set of buttons.
     */
    private void populateMenuButtons() {
        if (this.category != null) {
            ItemStack back = new ItemStack(Material.INK_SACK, 1, (short) 1);
            {
                ItemMeta meta = back.getItemMeta();
                meta.setDisplayName(this.plugin.getLocalizationManager().get("command.buy.back"));
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
                meta.setDisplayName(this.plugin.getLocalizationManager().get("command.buy.confirm"));
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
    @SuppressWarnings("deprecation")
    private void selectPackage(@Nonnull Package pkg) {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(this.plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(String.format(CART_ADD_ENDPOINT_URL, this.plugin.getConfiguration().getStoreName(), this.cartId)).openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(false);

                try (OutputStream outputStream = connection.getOutputStream()) {
                    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                        writer.write("packageId=" + pkg.getId());
                    }
                }

                connection.connect();

                Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                    this.selectedItem = true;
                    this.buildInterface();
                });
            } catch (IOException ex) {
                // TODO: Proper error handling
                this.getPlayer().sendMessage(this.plugin.getLocalizationManager().get("command.buy.failure"));
                ex.printStackTrace();
            }
        });

        // FIXME: Remove item from selection if single purchase only?
    }
}
