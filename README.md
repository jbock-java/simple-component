[![simple-component-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler/badge.svg?color=grey&subject=simple-component-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler)
[![simple-component](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component/badge.svg?subject=simple-component)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component)

A minimalistic approach to dependency injection. Basically the idea is that you can do (almost) everything dagger does with only a handful annotations:

1. `@Inject` declares an injection point, similarly `@Provides` declares an injection point within the component
2. `@Qualifier` and `@Named`
3. `@Component` along with `@Component.Factory`

There is no `@Scope`. Instead, you have a guarantee that every injection point gets invoked at most once per component instance.
Hence, the `@Singleton` annotation is unnecessary and is ignored.
There is no "prototype scope", which is the default scope in dagger..

There are no subcomponents or "component dependencies" (in dagger terms). You can still have more than one component though.

There is no `@Module` (in dagger terms); all `@Provides` methods must be static and live directly in the component.
There is no restriction on the return type of a `@Provides` method, as long as it does not return `void`.

There is no `@Binds` which, in dagger, does binding of a bean to its interface type.
It can be emulated with a simple `@Provides` method, or, if you control the source code of the interface, a static `@Inject` method.

There is no need for `@BindsInstance`. Each parameter of a `@Component.Factory` method is treated as if it had that annotation.

### Do more with less

* Works with both `javax.inject.Inject` or `jakarta.inject.Inject`.
* It also includes its own `@Inject` annotation, so you don't *have* to depend on one of these.
* Allows injection into static method.
* No typecasts in generated code, duh.
* Generates only the component implementation and nothing else, so this *should* be faster than dagger and doesn't bloat your jar as much.

The new feature, "injection into static method" is only allowed if the method's return value matches the type of the enclosing class.
For example, this is allowed because the method returns `Heater`:

```java
public interface Heater {
    @Inject
    static Heater getInstance() {
        return new ElectricHeater();
    }

    //...
}
```

In the following example, the method `setHeaterFactory` *could* be used to sneak in a mock `Heater` for testing purpose, if invoked before the component is created: 

```java
public interface Heater {

    private static Supplier<Heater> heaterFactory;

    @Inject
    static Heater getInstance() {
        if (heaterFactory == null) {
            heaterFactory = ElectricHeater::new;
        }
        return heaterFactory.get();
    }

    // only for testing
    static void setHeaterFactory(Supplier<Heater> mockHeaterFactory) {
        heaterFactory = mockHeaterFactory;
    }

    //...
}
```

You can have more than one static injection point in the same class, if you add a qualifier:

```java
public interface Heater {

    @Named("electric")
    @Inject
    static Heater createElectricHeater() {
        return ElectricHeater.getInstance();
    }

    @Named("diesel")
    @Inject
    static Heater createDieselHeater() {
        return DieselHeater.getInstance();
    }

    //...
}
```



### Samples

* [coffee example from dagger](https://github.com/jbock-java/modular-thermosiphon)
* [jbock](https://github.com/jbock-java/jbock) uses it, see for example [ValidateComponent](https://github.com/jbock-java/jbock/blob/master/compiler/src/main/java/net/jbock/validate/ValidateComponent.java)

### Alternatives

* https://github.com/google/dagger/
* https://github.com/avaje/avaje-inject
* https://github.com/michaelboyles/simple-di
