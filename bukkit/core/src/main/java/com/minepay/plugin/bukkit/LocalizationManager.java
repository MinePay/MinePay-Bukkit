package com.minepay.plugin.bukkit;

import org.bukkit.ChatColor;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides simplified access to localizations as well as support for switching the localization at
 * any given time.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class LocalizationManager {
    private final MinePayPlugin plugin;
    private Locale locale;
    private ResourceBundle sourceBundle;

    public LocalizationManager(@Nonnull MinePayPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieves a translation string from the manager.
     *
     * @param key a key.
     * @return a string.
     */
    @Nonnull
    public String get(@Nonnull String key, @Nonnull Object... arguments) {
        try {
            String value = ChatColor.translateAlternateColorCodes('&', this.sourceBundle.getString(key));

            if (arguments.length != 0) {
                return String.format(value, arguments);
            }

            return value;
        } catch (MissingResourceException ex) {
            return key;
        }
    }

    /**
     * Updates the current locale.
     *
     * @param locale a locale.
     * @return true if switched, false if switched to fallback.
     */
    public boolean setLocale(@Nullable Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }

        try {
            this.sourceBundle = ResourceBundle.getBundle("localization/minepay", locale, this.getClass().getClassLoader());
            this.locale = locale;

            return true;
        } catch (MissingResourceException ex) {
            // in case we cannot locate the requested locale, we'll fall back to regular English
            // since it has the best probability of being understood by the user
            this.plugin.getLogger().info("No localization is available for " + locale.getDisplayName() + " - Falling back to English");
            this.setLocale(Locale.ENGLISH);

            return false;
        }
    }
}
