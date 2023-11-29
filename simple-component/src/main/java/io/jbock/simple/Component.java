package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Annotates an interface for which a dependency-injected
 * implementation is to be generated. The generated class will
 * have the name of the type annotated, appended with {@code _Impl}. For
 * example, {@code @Component interface MyComponent {...}} will produce an implementation named
 * {@code MyComponent_Impl}.
 * <h2>Component methods
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
@Retention(SOURCE)
public @interface Component {

    /**
     * A factory for a component. Components <em>may</em> have a single nested {@code interface}
     * annotated with {@code @Component.Factory}.
     *
     * <p>A factory is an interface with a single method that returns a new component instance each time it
     * is called. The parameters of that method provide the bound instances
     * required by the component.
     */
    @Target(TYPE)
    @Retention(SOURCE)
    @interface Factory {
    }

    /**
     * A builder for a component. Components <em>may</em> have a single nested {@code interface}
     * annotated with {@code @Component.Builder}.
     *
     * <p>The builder is an interface with zero or more setter methods that return the builder type.
     * Additionally, there must be exactly one abstract no-argument method that returns the component
     * type, called the "build method". The setter methods provide the bound instances
     * required by the component.
     */
    @Retention(SOURCE)
    @Target(TYPE)
    @interface Builder {
    }

    /**
     * By default, the {@code mockBuilder} method is only package-private. This
     * should make it less prone to accidental invocation from production code.
     *
     * <p>In test code, this restriction can be circumvented by placing a public delegate class
     * in the component package.
     *
     * @return {@code true} if the {@code mockBuilder} method should have the same visibility
     * as the component.
     */
    boolean generatePublicMockBuilder() default false;

    /**
     * @return {@code true} if the {@code mockBuilder} method should not be generated.
     */
    boolean omitMockBuilder() default false;
}
