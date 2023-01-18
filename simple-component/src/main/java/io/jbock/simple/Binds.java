package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Example:
 *
 * <pre>{@code
 * @Module
 * interface HeaterModule {
 *   @Binds Heater bindHeater(ElectricHeater impl);
 * }
 * }</pre>
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface Binds {
}