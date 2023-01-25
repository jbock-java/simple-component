package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Annotates <em>abstract</em> methods of a {@link Module} that delegate bindings.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(METHOD)
public @interface Binds {
}