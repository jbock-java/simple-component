package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * <p>Annotates an interface for which a dependency-injected
 * implementation should be generated. The generated class will
 * have the name of the annotated interface, plus the suffix {@code _Impl}. For
 * example, if the annotated interface is called {@code interface MyComponent},
 * the generated implementation will be called
 * {@code class MyComponent_Impl implement MyComponent}.
 *
 * <h2>Component methods
 *
 * <p>The component interface must have at least one nullary abstract
 * method. The return type of each such method must be either
 *
 * <ul>
 *     <li>a class or record annotated with {@code @Inject}
 *     <li>a {@code @Provides}-annotated method's return type
 *     <li>the parameter type of one of the parameters of the SAM of a
 *         component factory interface
 *     <li>the parameter type of one of the setters of a
 *         component builder interface
 * </ul>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Component {

    /**
     * Annotation for a component factory interface. This interface must be nested directly
     * inside the component interface.
     *
     * <p>The factory interface must be a single-abstract-method interface, and its single method must return
     * the component type.
     * The runtime parameters of this method will be available for injection.
     *
     * <p>The implementation of the factory interface is immutable and can be re-used to create
     * more component instances.
     */
    @Target(TYPE)
    @Retention(SOURCE)
    @interface Factory {
    }

    /**
     * Annotation for a component builder interface. This interface must be nested directly
     * inside the component interface.
     *
     * <p>The builder interface can have any number of unary abstract methods which must return the builder type.
     * Additionally, there must be exactly one nullary abstract method which returns the component
     * type. The runtime parameters of the setter methods will be available for injection.
     *
     * <p>The implementation of the builder interface is immutable and can be re-used to create
     * more component instances.
     */
    @Retention(SOURCE)
    @Target(TYPE)
    @interface Builder {
    }
}
