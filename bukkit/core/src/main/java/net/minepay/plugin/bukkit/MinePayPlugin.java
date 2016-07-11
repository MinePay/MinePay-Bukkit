package net.minepay.plugin.bukkit;

import net.minepay.plugin.bukkit.boilerplate.BukkitBoilerplate;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * Provides an entry point
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class MinePayPlugin extends JavaPlugin {
    private final PluginConfiguration configuration = new PluginConfiguration();
    private final BukkitBoilerplate bukkitBoilerplate = BukkitBoilerplate.getInstance();

    @Nonnull
    public PluginConfiguration getConfiguration() {
        return this.configuration;
    }

    @Nonnull
    public BukkitBoilerplate getBukkitBoilerplate() {
        return this.bukkitBoilerplate;
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
            }
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not load the plugin configuration file: " + ex.getMessage(), ex);
        }

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
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        super.onDisable();
    }
}
