package com.minepay.plugin.bukkit.telemetry;

import org.json.simple.JSONObject;

import javax.annotation.Nonnull;

/**
 * Represents a single record submitted to the telemetry endpoint.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class TelemetryDataPoint {
    private final String name;

    protected TelemetryDataPoint(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    public static IntegerDataPoint createInteger(@Nonnull String name, int value) {
        return new IntegerDataPoint(name, value);
    }

    @Nonnull
    public static LongDataPoint createLong(@Nonnull String name, long value) {
        return new LongDataPoint(name, value);
    }

    @Nonnull
    public static FloatDataPoint createFloat(@Nonnull String name, float value) {
        return new FloatDataPoint(name, value);
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * Encodes this object into a serializable representation.
     *
     * @return an encoded object.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public JSONObject toEncodedObject() {
        JSONObject object = new JSONObject();
        object.put("name", this.name);
        return object;
    }

    /**
     * Represents a float based data point.
     */
    public static class FloatDataPoint extends TelemetryDataPoint {
        private final float value;

        protected FloatDataPoint(@Nonnull String name, float value) {
            super(name);
            this.value = value;
        }

        public float getValue() {
            return this.value;
        }

        @Nonnull
        @Override
        @SuppressWarnings("unchecked")
        public JSONObject toEncodedObject() {
            JSONObject object = super.toEncodedObject();
            object.put("value", this.value);
            return object;
        }
    }

    /**
     * Represents an integer based data point.
     */
    public static class IntegerDataPoint extends TelemetryDataPoint {
        private final int value;

        protected IntegerDataPoint(@Nonnull String name, int value) {
            super(name);
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        /**
         * {@inheritDoc}
         */
        @Nonnull
        @Override
        @SuppressWarnings("unchecked")
        public JSONObject toEncodedObject() {
            JSONObject object = super.toEncodedObject();
            object.put("value", this.value);
            return object;
        }
    }

    /**
     * Represents a long based data point.
     */
    public static class LongDataPoint extends TelemetryDataPoint {
        private final long value;

        protected LongDataPoint(@Nonnull String name, long value) {
            super(name);
            this.value = value;
        }

        public long getValue() {
            return this.value;
        }

        /**
         * {@inheritDoc}
         */
        @Nonnull
        @Override
        @SuppressWarnings("unchecked")
        public JSONObject toEncodedObject() {
            JSONObject object = super.toEncodedObject();
            object.put("value", this.value);
            return object;
        }
    }
}
