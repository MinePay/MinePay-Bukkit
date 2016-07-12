package com.minepay.plugin.bukkit;

import com.minepay.plugin.bukkit.boilerplate.BukkitBoilerplate;
import com.minepay.plugin.bukkit.boilerplate.CraftBukkitBoilerplate;
import com.minepay.plugin.bukkit.command.MinePayCommandExecutor;
import com.minepay.plugin.bukkit.task.TelemetryTask;
import com.minepay.plugin.bukkit.task.TickAverageTask;
import com.minepay.plugin.bukkit.task.TickCounterTask;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Provides an entry point to the MinePay Bukkit integration.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class MinePayPlugin extends JavaPlugin {
    private final PluginConfiguration configuration = new PluginConfiguration();
    private final BukkitBoilerplate bukkitBoilerplate = BukkitBoilerplate.getInstance();

    // we're storing an optional in this field in order to simplify code further down the road
    // this is generally not recommended so please don't just adapt this in your plugins like a
    // mindless zombie ...
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<CraftBukkitBoilerplate> craftBukkitBoilerplate = CraftBukkitBoilerplate.getInstance();

    private final TickCounterTask tickCounterTask = new TickCounterTask();
    private final TickAverageTask tickAverageTask = new TickAverageTask(this.tickCounterTask, this.craftBukkitBoilerplate.orElse(null));
    private final TelemetryTask telemetryTask = new TelemetryTask(this);
    private int tickCounterTaskId = -1;
    private int tickAverageTaskId = -1;
    private int telemetryTaskId = -1;

    @Nonnull
    public PluginConfiguration getConfiguration() {
        return this.configuration;
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
     * Enables the plugin functionality as soon as the authentication information is available.
     */
    public void enableFunctionality() {
        if (!this.craftBukkitBoilerplate.isPresent()) {
            this.tickCounterTaskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this.tickCounterTask, 1, 1);
        }

        this.tickAverageTaskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this.tickAverageTask, 50, 25);

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

        // register command executors
        this.getServer().getPluginCommand("minepay").setExecutor(new MinePayCommandExecutor(this));

        // warn user about missing configuration options
        if (this.configuration.getServerId().isEmpty()) {
            this.getLogger().warning("+===============================+");
            this.getLogger().warning("| No Server Configuration       |");
            this.getLogger().warning("+===============================+");
            this.getLogger().warning("| You did not specify any       |");
            this.getLogger().warning("| server identification yet!    |");
            this.getLogger().warning("|                               |");
            this.getLogger().warning("| The plugin will not work      |");
            this.getLogger().warning("| until you specify a server ID |");
            this.getLogger().warning("| using /mp serverId <serverId> |");
            this.getLogger().warning("+===============================+");
        } else {
            this.enableFunctionality();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        super.onDisable();
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
