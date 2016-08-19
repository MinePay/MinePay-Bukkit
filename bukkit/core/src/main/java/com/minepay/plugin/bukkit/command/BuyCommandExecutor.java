package com.minepay.plugin.bukkit.command;

import com.minepay.plugin.bukkit.MinePayPlugin;
import com.minepay.plugin.bukkit.storefront.Cart;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * Provides a simple command executor which will display the buy menu when a user requests it.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class BuyCommandExecutor implements CommandExecutor {
    private final MinePayPlugin plugin;

    public BuyCommandExecutor(@Nonnull MinePayPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] arguments) {
        if (commandSender instanceof ConsoleCommandSender) {
            commandSender.sendMessage(this.plugin.getLocalizationManager().get("command.buy.console"));
            return true;
        } else if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(this.plugin.getLocalizationManager().get("command.buy.unsupported"));
            return true;
        }

        Player player = (Player) commandSender;
        this.plugin.getCartManager().create(player, this::onCartCreated, () -> commandSender.sendMessage(this.plugin.getLocalizationManager().get("command.buy.failure")));

        return true;
    }

    /**
     * Handles the creation of a new cart.
     *
     * @param cart a cart.
     */
    private void onCartCreated(@Nonnull Cart cart) {
        cart.show();
    }
}
