package com.minepay.plugin.bukkit.task;

import com.minepay.plugin.bukkit.MinePayPlugin;
import com.minepay.plugin.bukkit.storefront.Category;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Periodically fetches an updates set of known packages available in the server store.
 * This data is used as part of the buy menu.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@ThreadSafe
public class PackageTask implements Runnable {
    private static final String CATEGORIES_ENDPOINT_URL = "https://api.minepay.net/v1/store/%s/categories";
    private static final String PACKAGES_ENDPOINT_URL = "https://api.minepay.net/v1/store/%s/category/%d";
    private final MinePayPlugin plugin;
    private final String storeName;
    private AtomicReference<List<Category>> categories = new AtomicReference<>();

    public PackageTask(@Nonnull MinePayPlugin plugin, @Nonnull String storeName) {
        this.plugin = plugin;
        this.storeName = storeName;
    }

    /**
     * Retrieves a list of registered categories.
     *
     * @return a list of categories.
     */
    @Nonnull
    public List<Category> getCategories() {
        return this.categories.get();
    }

    /**
     * Fetches a single category and its packages from the servers.
     *
     * @param categoryId a category identifier.
     * @return a category.
     *
     * @throws IllegalStateException when an invalid response is received.
     * @throws IOException           when an error occurs while fetching data.
     * @throws ParseException        when parsing the retrieved data fails.
     */
    @Nonnull
    private Category fetchCategory(@Nonnegative long categoryId) throws IllegalStateException, IOException, ParseException {
        JSONParser parser = new JSONParser();
        HttpURLConnection connection = (HttpURLConnection) new URL(String.format(PACKAGES_ENDPOINT_URL, this.storeName, categoryId)).openConnection();

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            if (responseCode >= 500) {
                throw new IllegalStateException("The MinePay servers are currently unavailable");
            } else {
                throw new IllegalStateException("Expected response code 200 but received " + responseCode);
            }
        } else {
            try (InputStream inputStream = connection.getInputStream()) {
                try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                    JSONObject object = (JSONObject) parser.parse(reader);

                    return new Category(object);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        JSONParser parser = new JSONParser();

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(CATEGORIES_ENDPOINT_URL, this.storeName)).openConnection();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                if (responseCode >= 400 && responseCode < 500) {
                    this.plugin.getLogger().severe("Could not fetch categories: Expected response code 200 but received " + responseCode);
                } else if (responseCode >= 500) {
                    this.plugin.getLogger().warning("Could not fetch categories: The MinePay servers are currently unavailable");
                }
            } else {
                List<Category> categories = new LinkedList<>();

                try (InputStream inputStream = connection.getInputStream()) {
                    try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                        JSONArray array = (JSONArray) parser.parse(reader);

                        for (Object obj : array) {
                            JSONObject object = (JSONObject) obj;

                            try {
                                categories.add(this.fetchCategory((long) object.get("id")));
                            } catch (IllegalArgumentException | ParseException ex) {
                                this.plugin.getLogger().log(Level.SEVERE, "Could not fetch details for category: " + ex.getMessage());
                            }
                        }
                    }
                }

                this.categories.set(categories);
            }
        } catch (IOException | ParseException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not fetch store categories: " + ex.getMessage(), ex);
        }
    }
}
