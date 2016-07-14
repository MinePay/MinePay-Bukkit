package com.minepay.plugin.bukkit.command.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;

/**
 * Provides a simple marker annotation in order to simplify command registrations.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandHandler {

    /**
     * Defines a general usage of command arguments which is displayed to users when invoking the
     * command without arguments.
     *
     * @return a command usage.
     */
    @Nonnull
    String usage() default "";

    /**
     * Defines a set of known sub commands which are displayed in the generated help message.
     *
     * @return a set of commands.
     */
    @Nonnull
    String[] subCommands() default {};
}
