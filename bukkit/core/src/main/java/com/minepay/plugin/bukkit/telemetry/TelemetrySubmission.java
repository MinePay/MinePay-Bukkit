package com.minepay.plugin.bukkit.telemetry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Represents a telemetry submission of multiple records.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class TelemetrySubmission {
    private final List<TelemetryDataPoint> dataPoints = new ArrayList<>();

    public void addDatapoint(@Nonnull TelemetryDataPoint datapoint) {
        this.dataPoints.add(datapoint);
    }

    @Nonnull
    public List<TelemetryDataPoint> getDatapoints() {
        return Collections.unmodifiableList(this.dataPoints);
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
                .map(TelemetryDataPoint::toEncodedObject)
                .forEach(array::add);

        object.put("datapoints", array);
        return object;
    }
}
