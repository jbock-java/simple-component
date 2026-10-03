package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Creates a binding in the component tree.
 *
 * <p>This annotation can be used on records, classes and constructors.
 *
 * <p>If used on a class, the class must have exactly one constructor.
 *
 * <p>For a static method in the component interface,
 *    the {@link Provides} annotation serves the same purpose.
 */
@Target({TYPE, CONSTRUCTOR})
@Retention(RUNTIME)
public @interface Inject {
}
