package io.jbock.simple;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation goes on an interface.
 *
 * <p>Each interface method must have a single parameter.
 * The parameter type must resolve to an {@code Inject}-annotated method or constructor,
 * and it must be assignable to the method's return type.
 *
 * <p>Note: In dagger terms, a type alias method corresponds to a module method
 * that has a "{@code @Binds}" annotation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TypeAliases {
}