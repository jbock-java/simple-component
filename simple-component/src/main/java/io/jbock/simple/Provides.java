package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Creates a binding in the component tree.
 *
 * <p>This alternative to the {@code @Inject} annotation
 *    can only be used on static methods in the component interface.
 *    The parameters of this method will be injected.
 *    The return value of this method will be available for injection.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Provides {
}
