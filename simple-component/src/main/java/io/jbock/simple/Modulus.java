package io.jbock.simple;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class that contributes to the object graph.
 * A binding is a {@code static} methods that is annotated with either
 * {@code @Provides} or {@code @Inject}.
 *
 * <p>The annotated class must also be referenced in the
 * {@code modules} of a {@link Component}.
 *
 * <p>This class is called {@code Modulus}, not {@code Module}
 * because there is a class {@code Module} in {@code java.lang}
 * since Java 9.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Modulus {
}