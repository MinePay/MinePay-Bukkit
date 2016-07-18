package com.minepay.plugin.bukkit.storefront;

import org.json.simple.JSONObject;

import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * Represents a set of possible Cart related responses from the API.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public abstract class CartResponse {
    private final UUID id;

    public CartResponse(@Nonnull UUID id) {
        this.id = id;
    }

    public CartResponse(@Nonnull JSONObject object) {
        this.id = UUID.fromString((String) object.get("cartID"));
    }

    @Nonnull
    public UUID getId() {
        return this.id;
    }

    public static class Create extends CartResponse {
        private final String username;

        public Create(@Nonnull UUID id, @Nonnull String username) {
            super(id);
            this.username = username;
        }

        public Create(@Nonnull JSONObject object) {
            super(object);
            this.username = (String) object.get("username");
        }

        @Nonnull
        public String getUsername() {
            return this.username;
        }
    }

    public static class Add extends CartResponse {
        private final long packageId;

        public Add(@Nonnull UUID id, long packageId) {
            super(id);
            this.packageId = packageId;
        }

        public Add(@Nonnull JSONObject object) {
            super(object);
            this.packageId = (long) object.get("packageAdded");
        }

        @Nonnull
        public long getPackageId() {
            return this.packageId;
        }
    }

    public static class Information {
        private final String username;
        private final String url;

        public Information(@Nonnull String username, @Nonnull String url) {
            this.username = username;
            this.url = url;
        }

        public Information(@Nonnull JSONObject object) {
            this.username = (String) object.get("username");
            this.url = (String) object.get("cartURL");
        }

        @Nonnull
        public String getUsername() {
            return this.username;
        }

        @Nonnull
        public String getUrl() {
            return this.url;
        }
    }
}
