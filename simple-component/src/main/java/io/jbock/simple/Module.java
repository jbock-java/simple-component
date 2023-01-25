package io.jbock.simple;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation goes on an interface.
 *
 * <p>Each interface method must have a single parameter.
 * The parameter must be of a bound type, and it must be assignable
 * to the method's return type.
 *
 * <p>Each interface method must also have a {@link Binds @Binds} annotation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Module {
}