package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * An alternative to the {@code @Inject} annotation that can be used
 * on static methods which are direct children of the component interface.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Provides {
}
