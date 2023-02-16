package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Identifies injectable constructors and <em>static</em> methods.
 */
@Target({METHOD, CONSTRUCTOR})
@Retention(SOURCE)
public @interface Inject {
}