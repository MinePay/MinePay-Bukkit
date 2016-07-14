package com.minepay.plugin.bukkit;

import com.minepay.plugin.bukkit.boilerplate.BukkitBoilerplate;
import com.minepay.plugin.bukkit.boilerplate.CraftBukkitBoilerplate;
import com.minepay.plugin.bukkit.command.ConfigurationCommandExecutor;
import com.minepay.plugin.bukkit.task.TelemetryTask;
import com.minepay.plugin.bukkit.task.TickAverageTask;
import com.minepay.plugin.bukkit.task.TickCounterTask;
import com.minepay.plugin.bukkit.telemetry.Submission;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.logging.Level;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides an entry point to the MinePay Bukkit integration.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class MinePayPlugin extends JavaPlugin {
    private final PluginConfiguration configuration = new PluginConfiguration();
    private final LocalizationManager localizationManager = new LocalizationManager(this);
    private final BukkitBoilerplate bukkitBoilerplate = BukkitBoilerplate.getInstance();

    // we're storing an optional in this field in order to simplify code further down the road
    // this is generally not recommended so please don't just adapt this in your plugins like a
    // mindless zombie ...
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<CraftBukkitBoilerplate> craftBukkitBoilerplate = CraftBukkitBoilerplate.getInstance();
    private final TickCounterTask tickCounterTask = new TickCounterTask();
    private final TickAverageTask tickAverageTask = new TickAverageTask(this.tickCounterTask, this.craftBukkitBoilerplate.orElse(null));
    private final TelemetryTask telemetryTask = new TelemetryTask(this);
    private HikariDataSource dataSource;
    private int tickCounterTaskId = -1;
    private int tickAverageTaskId = -1;
    private int telemetryTaskId = -1;

    @Nonnull
    public LocalizationManager getLocalizationManager() {
        return this.localizationManager;
    }

    @Nonnull
    public PluginConfiguration getConfiguration() {
        return this.configuration;
    }

    @Nonnull
    public HikariDataSource getDataSource() {
        return this.dataSource;
    }

    @Nonnull
    public BukkitBoilerplate getBukkitBoilerplate() {
        return this.bukkitBoilerplate;
    }

    @Nonnull
    public Optional<CraftBukkitBoilerplate> getCraftBukkitBoilerplate() {
        return this.craftBukkitBoilerplate;
    }

    /**
     * Creates the initial database schema.
     */
    private void createDatabaseSchema() {
        this.getLogger().info("No command queue database found - Creating a new schema");

        try (Connection connection = this.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                stmt.addBatch(
                        "CREATE TABLE command_queue (" +
                                "template TEXT NOT NULL," +
                                "profileId VARCHAR(36) NOT NULL" +
                                ")"
                );

                stmt.executeBatch();
            }

            connection.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Could not create initial database schema: " + ex.getMessage(), ex);
        }
    }

    /**
     * Enables the plugin functionality as soon as the authentication information is available.
     */
    public void enableFunctionality() {
        if (!this.craftBukkitBoilerplate.isPresent() && this.tickCounterTaskId == -1) {
            this.tickCounterTaskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this.tickCounterTask, 1, 1);
        }

        if (this.tickAverageTaskId == -1) {
            this.tickAverageTaskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this.tickAverageTask, 50, 25);
        }

        if (this.configuration.isTelemetryEnabled()) {
            this.enableTelemetry();
        }
    }

    /**
     * Disables the plugin functionality when the user temporarily disables synchronization or
     * chooses to un-register their server.
     *
     * TODO: This method should also be called when the API becomes un-available for longer periods
     * of time.
     */
    public void disableFunctionality() {
        this.disableTelemetry();

        if (this.tickCounterTaskId != -1) {
            this.getServer().getScheduler().cancelTask(this.tickCounterTaskId);
            this.tickCounterTaskId = -1;
        }

        this.getServer().getScheduler().cancelTask(this.tickAverageTaskId);
        this.tickAverageTaskId = -1;
    }

    /**
     * Enables the submission of telemetry data.
     */
    public void enableTelemetry() {
        if (this.telemetryTaskId != -1) {
            return;
        }

        this.telemetryTaskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this.telemetryTask, 600, 600);
        this.getLogger().info("Telemetry submission is now enabled");
    }

    /**
     * Disables the submission of telemetry data.
     */
    public void disableTelemetry() {
        if (this.telemetryTaskId == -1) {
            return;
        }

        this.getServer().getScheduler().cancelTask(this.telemetryTaskId);
        this.telemetryTaskId = -1;

        this.getLogger().info("Telemetry submission has been disabled");
    }

    /**
     * Retrieves the most recent telemetry submission.
     *
     * @return a submission.
     */
    @Nullable
    public Submission getLatestSubmission() {
        return this.telemetryTask.getSubmission();
    }

    /**
     * Retrieves the average amount of ticks processed in a second by the server.
     *
     * @return a tick average.
     */
    @Nonnegative
    public float getTickAverage() {
        return this.tickAverageTask.getAverage();
    }

    /**
     * Checks whether telemetry is currently active.
     *
     * @return true if enabled, false otherwise.
     */
    public boolean isTelemetryEnabled() {
        return this.telemetryTaskId != -1 && this.configuration.isTelemetryEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable() {
        super.onEnable();

        // load plugin configuration
        this.getDataFolder().mkdirs();
        boolean databaseCreated = Files.exists(this.getDataFolder().toPath().resolve("queue.db"));

        this.dataSource = new HikariDataSource();
        this.dataSource.setDriverClassName("org.sqlite.JDBC");
        this.dataSource.setConnectionTestQuery("SELECT 1");
        this.dataSource.setJdbcUrl("jdbc:sqlite:/" + this.getDataFolder().toPath().resolve("queue.db").toAbsolutePath().toString());
        this.dataSource.setUsername("minepay");
        this.dataSource.setPassword("storage");

        if (!databaseCreated) {
            this.createDatabaseSchema();
        }

        try {
            this.configuration.load(this.getDataFolder().toPath());
        } catch (FileNotFoundException ex) {
            this.getLogger().info("No configuration file found. Using defaults.");

            try {
                this.configuration.save(this.getDataFolder().toPath());
            } catch (IOException e) {
                this.getLogger().log(Level.SEVERE, "Could not save the plugin configuration file: " + e.getMessage(), e);
                throw new RuntimeException("Could not save the plugin configuration file", e);
            }
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not load the plugin configuration file: " + ex.getMessage(), ex);
            throw new RuntimeException("Could not load plugin configuration file", ex);
        }

        this.localizationManager.setLocale(this.configuration.getLocale());

        // register command executors
        this.getServer().getPluginCommand("minepay").setExecutor(new ConfigurationCommandExecutor(this));

        // warn user about missing configuration options
        if (this.configuration.getServerId().isEmpty()) {
            String[] message = WordUtils.wrap(this.localizationManager.get("warning.startup.unregistered"), 42).replace("\r", "").split("\n");
            this.getLogger().warning("+--------------------------------------------+");

            for (String line : message) {
                this.getLogger().warning("| " + StringUtils.rightPad(line, 42) + " |");
            }

            this.getLogger().warning("+--------------------------------------------+");
        } else {
            this.enableFunctionality();
        }

        // check for NMS compatibility
        if (!this.craftBukkitBoilerplate.isPresent()) {
            this.getLogger().warning(this.localizationManager.get("warning.startup.nms", Bukkit.getBukkitVersion()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        super.onDisable();

        this.dataSource.close();
    }

    /**
     * Attempts to save the configuration file back to disk and reports any errors that arise to the
     * user via the server console.
     */
    public void saveConfiguration() {
        try {
            this.configuration.save(this.getDataFolder().toPath());
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save plugin configuration file: " + ex.getMessage(), ex);
        }
    }
}
