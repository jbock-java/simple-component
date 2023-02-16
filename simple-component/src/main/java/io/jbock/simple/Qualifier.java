package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies qualifier annotations.
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
public @interface Qualifier {
}