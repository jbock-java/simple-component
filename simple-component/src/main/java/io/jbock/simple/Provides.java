package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * This is an alternative to the {@code @Inject} annotation
 * which can only be used on static methods directly in the component.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Provides {
}
