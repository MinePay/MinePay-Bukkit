package com.minepay.plugin.bukkit.task;

import com.minepay.plugin.bukkit.boilerplate.CraftBukkitBoilerplate;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

/**
 * Calculates the average of ticks processed per second on this server.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class TickAverageTask implements Runnable {
    private final TickCounterTask counterTask;
    private final CraftBukkitBoilerplate craftBukkitBoilerplate;
    private int lastTickCount = 0;
    private float average = 20.0f;
    private Instant lastCalculation;

    public TickAverageTask(@Nullable TickCounterTask counterTask, @Nullable CraftBukkitBoilerplate craftBukkitBoilerplate) {
        this.counterTask = counterTask;
        this.craftBukkitBoilerplate = craftBukkitBoilerplate;
    }

    /**
     * Retrieves the average amount of ticks processed per second within the sample period.
     *
     * @return an average.
     */
    @Nonnegative
    public float getAverage() {
        return this.average;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        // counterTask is guaranteed to be filled with a reference when no CraftBukkit integration
        // is available by the core plugin class
        @SuppressWarnings("ConstantConditions")
        int newCount = (this.craftBukkitBoilerplate != null ? this.craftBukkitBoilerplate.getTickCount() : this.counterTask.getTickCount());
        Instant newCalculation = Instant.now();

        if (this.lastCalculation != null) {
            int difference = newCount - this.lastTickCount;
            Duration duration = Duration.between(this.lastCalculation, newCalculation);

            this.average = difference / (float) duration.getSeconds();
        }

        this.lastTickCount = newCount;
        this.lastCalculation = newCalculation;
    }
}
