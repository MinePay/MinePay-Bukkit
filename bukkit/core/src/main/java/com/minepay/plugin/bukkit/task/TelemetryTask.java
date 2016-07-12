package com.minepay.plugin.bukkit.task;

import com.minepay.plugin.bukkit.MinePayPlugin;
import com.minepay.plugin.bukkit.telemetry.TelemetryDataPoint;
import com.minepay.plugin.bukkit.telemetry.TelemetrySubmission;

import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Submits telemetry data to the MinePay servers for statistical purposes.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class TelemetryTask implements Runnable {
    public static final String TELEMETRY_ENDPOINT_URL = "https://api.minepay.net/v1/telemetry";
    private final MinePayPlugin plugin;
    private TelemetrySubmission submission;

    public TelemetryTask(@Nonnull MinePayPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieves the last telemetry submission.
     *
     * @return a submission.
     */
    @Nullable
    public TelemetrySubmission getSubmission() {
        return this.submission;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation") // Bukkit idiotism
    public void run() {
        TelemetrySubmission.Builder builder = TelemetrySubmission.builder();

        builder.add(TelemetryDataPoint.createLong("ram-free", Runtime.getRuntime().freeMemory()));
        builder.add(TelemetryDataPoint.createLong("ram-max", Runtime.getRuntime().maxMemory()));
        builder.add(TelemetryDataPoint.createLong("ram-total", Runtime.getRuntime().totalMemory()));
        builder.add(TelemetryDataPoint.createInteger("players-current", this.plugin.getBukkitBoilerplate().getOnlinePlayers().size()));
        builder.add(TelemetryDataPoint.createFloat("tps", this.plugin.getTickAverage()));

        TelemetrySubmission submission = builder.build();
        this.submission = submission;

        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, () -> this.submit(submission));
    }

    /**
     * Submits a set of data points to the server.
     *
     * @param submission a set of data points.
     */
    public void submit(@Nonnull TelemetrySubmission submission) {
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(TELEMETRY_ENDPOINT_URL)).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;Charset=UTF-8");
            connection.setRequestProperty("User-Agent", "MinePay Bukkit Plugin (+https://www.minepay.com)");
            connection.setRequestProperty("X-ServerId", this.plugin.getConfiguration().getServerId());
            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(submission.toEncodedObject().toJSONString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 400) {
                return;
            }

            if (responseCode >= 400 && responseCode < 500) {
                this.plugin.getLogger().severe("Could not push telemetry data: Expected response code 200 but received " + responseCode);
            } else if (responseCode >= 500) {
                this.plugin.getLogger().warning("Could not push telemetry data: The MinePay servers are currently unavailable");
            }
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not push telemetry data: " + ex.getMessage(), ex);
        }
    }
}
