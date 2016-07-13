package com.minepay.plugin.bukkit.event;

import com.minepay.plugin.bukkit.MinePayPlugin;
import com.minepay.plugin.bukkit.command.CommandTemplate;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class CommandEventListener implements Listener {
    private final MinePayPlugin plugin;

    public CommandEventListener(@Nonnull MinePayPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles login events in order to issue pending commands for players who weren't online when
     * the command was polled from the MinePay queue.
     *
     * @param event an event.
     */
    @SuppressWarnings("deprecation") // Bukkit stupidity
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLogin(@Nonnull PlayerLoginEvent event) {
        // while modern Bukkit implementations will move login events to a separate thread in order
        // to prevent locking up of network threads, we'll rely on the scheduler in this case since
        // we aim to support as many versions as possible
        // In addition, this behavior is not properly documented and thus not part of the API
        // contract and should thus not be relied upon
        this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new CommandHelper(event.getPlayer()));
    }

    /**
     * Provides a helper class which is capable of finding the commands which have been queued on
     * behalf of a player.
     */
    private class CommandHelper implements Runnable {
        private final Player player;

        CommandHelper(@Nonnull Player player) {
            this.player = player;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try (Connection connection = CommandEventListener.this.plugin.getDataSource().getConnection()) {
                List<CommandTemplate> templates = new ArrayList<>();

                try (PreparedStatement stmt = connection.prepareStatement("SELECT template FROM command_queue WHERE profileId = ?")) {
                    // FIXME: This method of retrieving a player's UUID or name might not be safe due to
                    // modifications occurring on other threads such as the main thread
                    stmt.setString(1, this.player.getUniqueId().toString());

                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            templates.add(new CommandTemplate(this.player, resultSet.getString("template")));
                        }
                    }
                }

                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM command_queue WHERE profileId = ?")) {
                    stmt.setString(1, this.player.getUniqueId().toString());
                    stmt.execute();
                }

                templates.forEach(CommandTemplate::execute);
            } catch (SQLException ex) {
                CommandEventListener.this.plugin.getLogger().log(Level.SEVERE, "Could not retrieve queued commands for player " + this.player.getDisplayName() + ": " + ex.getMessage(), ex);
            }
        }
    }
}
