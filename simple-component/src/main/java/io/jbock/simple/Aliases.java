package io.jbock.simple;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation goes on an interface.
 *
 * <p>Each interface method must have a single parameter of a bound type.
 * The parameter type must be assignable to the return type.
 *
 * <p>Note: In dagger terms, an alias method is similar to a module method
 * with a "{@code @Binds}" annotation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Aliases {
}