package io.jbock.simple;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * <p>Annotates an interface for which a dependency-injected
 * implementation is to be generated. The generated class will
 * have the name of the type annotated, appended with {@code _Impl}. For
 * example, {@code @Component interface MyComponent {...}} will produce an implementation named
 * {@code MyComponent_Impl}.</p>
 *
 * <h2>Component methods</h2>
 *
 * <p>Every type annotated with {@code @Component} must contain at least one abstract component
 * method. Component methods may have any name, but must have no parameters and return a bound type.
 * A bound type is one of the following:</p>
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
     *
     * <p>The generated implementation of the factory will be immutable.
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
     *
     * <p>If the {@code mockBuilder} attribute is {@code true}, then the generated implementation
     * of the builder will contain an additional method called {@code withMocks} which
     * returns a new {@code MockBuilder}.
     */
    @Retention(SOURCE)
    @Target(TYPE)
    @interface Builder {
    }

    /**
     * If {@code true}, the generated component implementation will contain
     * a static {@code mockBuilder} method. However, if this component uses a {@code Builder},
     * the {@code mockBuilder} method will not be generated; see {@linkplain Builder}.
     *
     * @return {@code true} if the {@code mockBuilder} method should be generated.
     */
    boolean mockBuilder() default false;

    /**
     * By default, the {@code mockBuilder} (or {@code withMocks}) method is only package-private.
     * This makes it harder to accidentally invoke from production code.
     *
     * <p>In test code, {@code mockBuilder} can always be invoked, even if it is only package-visible,
     * by placing a forwarding delegate class in the correct package.
     * For example, if {@code MyComponent} is defined in package {@code com.my.component},
     * the forwarding delegate class could live in {@code src/test/java/com/my/component} and look
     * like this:
     *
     * <pre>{@code
     * public class MyComponentAccess {
     *   public static MyComponent_Impl.MockBuilder mockBuilder() {
     *       return MyComponent_Impl.mockBuilder();
     *   }
     * }
     * }</pre>
     *
     * @return {@code true} if the {@code mockBuilder} (or {@code withMocks}) method
     * should have the same visibility as the component.
     */
    boolean publicMockBuilder() default false;
}
