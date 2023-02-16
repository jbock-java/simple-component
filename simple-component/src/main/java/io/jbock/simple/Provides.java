package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Annotates <em>static</em> methods of a {@linkplain Component component}
 * to create a provider method binding.
 * 
 * <p>The method's return type is bound to its returned value.
 * 
 * <p>The {@linkplain Component component}
 * implementation will pass dependencies to the method as parameters.
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface Provides {
}
