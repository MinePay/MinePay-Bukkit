package com.minepay.plugin.bukkit.task;

import javax.annotation.Nonnegative;

/**
 * Keeps track of all ticks that have been processed by this server since the plugin has been
 * loaded.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class TickCounterTask implements Runnable {
    private int counter;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        ++this.counter;
    }

    /**
     * Retrieves the overall amount of ticks processed by this server since this plugin has been
     * loaded.
     *
     * @return an amount of ticks.
     */
    @Nonnegative
    public int getTickCount() {
        return this.counter;
    }
}
