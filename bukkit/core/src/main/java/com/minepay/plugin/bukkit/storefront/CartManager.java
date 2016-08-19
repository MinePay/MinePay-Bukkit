package com.minepay.plugin.bukkit.storefront;

import com.minepay.plugin.bukkit.MinePayPlugin;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages all active instances of carts and provisions new instances as needed.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class CartManager implements Listener {
    public static final String CART_CREATE_ENDPOINT_URL = "https://api.minepay.net/v1/store/examplestore/cart/%s";

    private final MinePayPlugin plugin;
    private final Map<Player, Cart> cartMap = new HashMap<>();

    public CartManager(@Nonnull MinePayPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player disconnections in order to clean up resources.
     *
     * @param event an event.
     */
    public void onPlayerQuit(@Nonnull PlayerQuitEvent event) {
        this.cartMap.remove(event.getPlayer());
    }

    /**
     * Retrieves a pre-existing cart.
     *
     * @param player a player.
     * @return a cart.
     */
    @Nullable
    public Cart getCart(@Nonnull Player player) {
        return this.cartMap.get(player);
    }

    /**
     * Creates a new cart for a specified player.
     *
     * @param player          a player.
     * @param successCallback a success callback.
     * @param failureCallback a failure callback.
     */
    @SuppressWarnings("deprecation")
    public void create(@Nonnull Player player, @Nonnull Consumer<Cart> successCallback, @Nonnull Runnable failureCallback) {
        final List<Category> categories = this.plugin.getCategories();

        if (categories == null) {
            failureCallback.run();
            return;
        }

        this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, () -> {
            final JSONParser parser = new JSONParser();

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(String.format(CART_CREATE_ENDPOINT_URL, URLEncoder.encode(player.getName(), "UTF-8"))).openConnection();

                int responseCode = connection.getResponseCode();
                if (responseCode >= 500) {
                    throw new IllegalStateException("The Minepay servers are currently unavailable");
                } else if (responseCode != 200) {
                    throw new IllegalStateException("Expected status code 200 but received " + responseCode);
                }

                try (InputStream inputStream = connection.getInputStream()) {
                    try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                        JSONObject object = (JSONObject) parser.parse(reader);

                        final CartResponse.Create response = new CartResponse.Create(object);
                        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                            final Cart cart = new Cart(this.plugin, player, response.getId(), categories);

                            this.cartMap.put(player, cart);
                            successCallback.accept(cart);
                        });
                    }
                }
            } catch (IllegalStateException | IOException | ParseException ex) {
                this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                    this.plugin.getLogger().log(Level.SEVERE, "Could not create cart for player " + player.getDisplayName() + ": " + ex.getMessage(), ex);
                    failureCallback.run();
                });
            }
        });
    }

    /**
     * Destroys a cart.
     *
     * @param player a player.
     */
    public void destroy(@Nonnull Player player) {
        this.cartMap.remove(player);
    }
}
