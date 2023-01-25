package io.jbock.simple;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Target(TYPE)
@Retention(SOURCE)
public @interface Component {

    Class<?> module() default Object.class;

    /**
     * This annotation goes on an interface.
     * The interface definition must be nested inside the component interface.
     * The factory interface must have exactly one interface method.
     * The factory method's return type must match the type of the component class.
     */
    @Retention(SOURCE)
    @Target(TYPE)
    @Documented
    @interface Factory {
    }
}
