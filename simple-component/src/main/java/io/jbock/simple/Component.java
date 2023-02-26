package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates an interface for which a dependency-injected
 * implementation is to be generated. The generated class will
 * have the name of the type annotated, appended with {@code _Impl}. For
 * example, {@code @Component interface MyComponent {...}} will produce an implementation named
 * {@code MyComponent_Impl}.
 * 
 * <h2>Component methods</h2>
 *
 * <p>Every type annotated with {@code @Component} must contain at least one abstract component
 * method. Component methods may have any name, but must have no parameters and return a bound type.
 * A bound type is one of the following:
 * 
 * <ul>
 *     <li>an {@link Inject injected} type
 *     <li>a {@link Provides provided} type
 *     <li>the type of one of the parameters of the {@link Component.Factory factory method}
 *     <li>{@code Provider<T>}, where {@code T} is one of the types described above
 * </ul>
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Component {

    /**
     * A factory for a component.Components <em>may</em> have a single nested {@code interface}
     * annotated with {@code @Component.Factory}.
     *
     * <p>A factory is a type with a single method that returns a new component instance each time it
     * is called. The parameters of that method allow the caller to provide the bound instances
     * required by the component.
     *
     * <p>Components may have a single nested {@code interface}
     * annotated with {@code @Component.Factory}. Factory types must follow some rules:
     *
     * <ul>
     *   <li>There must be exactly one abstract method, which must return the component type.
     *   <li>The method parameters bind the instance passed for that parameter within the component.
     * </ul>
     */
    @Target(TYPE)
    @Retention(RUNTIME)
    @interface Factory {
    }
}
