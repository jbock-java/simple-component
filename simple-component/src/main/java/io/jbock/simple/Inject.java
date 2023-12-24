package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies injectable constructors and {@code static} methods.
 * An {@code @Inject}-annotated static method is allowed in the following places:
 *
 * <ul>
 *   <li>The method can be a direct child of the class or interface
 *     that defines its return type.
 *   <li>If the class or interface that defines its return type is nested
 *     inside an enclosing class, then the method may also be a direct child of the
 *     enclosing class.
 *   <li>The method can be a direct child of the component interface.
 *     Alternatively, the {@link Provides} annotation can be used in that case.
 * </ul>
 */
@Target({METHOD, CONSTRUCTOR})
@Retention(RUNTIME)
public @interface Inject {
}