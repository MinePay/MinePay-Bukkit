package com.minepay.plugin.bukkit.telemetry;

import com.google.common.collect.ImmutableList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents a telemetry submission of multiple records.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@ThreadSafe
public final class Submission implements Iterable<DataPoint> {
    private final List<DataPoint> dataPoints;
    private final LocalDateTime generationTimestamp = LocalDateTime.now();

    private Submission(@Nonnull List<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    /**
     * Creates a new submission builder.
     *
     * @return a builder.
     */
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Retrieves the submission generation timestamp.
     *
     * @return an instant.
     */
    @Nonnull
    public LocalDateTime getGenerationTimestamp() {
        return this.generationTimestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<DataPoint> iterator() {
        return this.dataPoints.iterator();
    }

    /**
     * Converts the object into its serializable form.
     *
     * @return an encoded object.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public JSONObject toEncodedObject() {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();

        this.dataPoints.stream()
                .map(DataPoint::toEncodedObject)
                .forEach(array::add);

        object.put("datapoints", array);
        return object;
    }

    /**
     * Provides a builder for telemetry submission objects.
     */
    @NotThreadSafe
    public final static class Builder {
        private final ArrayList<DataPoint> dataPoints = new ArrayList<>();

        private Builder() {
        }

        /**
         * Builds a telemetry submission object.
         *
         * @return a submission.
         */
        @Nonnull
        public Submission build() {
            try {
                return new Submission(ImmutableList.copyOf(this.dataPoints));
            } finally {
                this.reset();
            }
        }

        /**
         * Resets the submission builder.
         */
        public void reset() {
            this.dataPoints.clear();
        }

        /**
         * Appends a data point to the set.
         *
         * @param dataPoint a data point.
         */
        public void add(@Nonnull DataPoint dataPoint) {
            this.dataPoints.add(dataPoint);
        }

        /**
         * Removes a data point from the set.
         *
         * @param dataPoint a data point.
         */
        public void remove(@Nonnull DataPoint dataPoint) {
            this.dataPoints.remove(dataPoint);
        }
    }
}
