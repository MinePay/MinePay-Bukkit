package net.minepay.plugin.bukkit;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * Represents the plugin configuration.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class PluginConfiguration {
    public static final String CONFIGURATION_FILE_NAME = "configuration.properties";

    private String serverId = "";
    private boolean telemetryEnabled = true;

    @Nonnull
    public String getServerId() {
        return this.serverId;
    }

    public void setServerId(@Nonnull String serverId) {
        this.serverId = serverId;
    }

    public boolean isTelemetryEnabled() {
        return this.telemetryEnabled;
    }

    public void setTelemetryEnabled(boolean telemetryEnabled) {
        this.telemetryEnabled = telemetryEnabled;
    }

    /**
     * Attempts to load a set of configuration options which have previously been stored in a file
     * by the plugin or the server administrator.
     *
     * @param baseDirectory the storage base directory.
     * @throws IOException when reading the configuration fails.
     */
    public void load(@Nonnull Path baseDirectory) throws IOException {
        Properties properties = new Properties();

        try (FileInputStream inputStream = new FileInputStream(baseDirectory.resolve(CONFIGURATION_FILE_NAME).toFile())) {
            properties.load(inputStream);
        }

        this.serverId = properties.getProperty("connection.serverId", "");
        this.telemetryEnabled = !Boolean.valueOf(properties.getProperty("telemetry.opt-out", "false"));
    }

    /**
     * Saves the current configuration options back to the file.
     *
     * @param baseDirectory the storage base directory.
     * @throws IOException when saving the configuration fails.
     */
    public void save(@Nonnull Path baseDirectory) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("connection.serverId", this.serverId);
        properties.setProperty("telemetry.opt-out", Boolean.toString(!this.telemetryEnabled));

        try (FileOutputStream outputStream = new FileOutputStream(baseDirectory.resolve(CONFIGURATION_FILE_NAME).toFile())) {
            properties.store(outputStream, "");
        }
    }
}
