package io.jbock.simple;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Target(TYPE)
@Retention(SOURCE)
public @interface Component {

    Class<?> aliases() default Object.class;

    Class<?> aliasOverrides() default Object.class;

    @Retention(SOURCE)
    @Target(TYPE)
    @Documented
    @interface Factory {
    }
}
