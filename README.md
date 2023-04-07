[![simple-component-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler/badge.svg?color=grey&subject=simple-component-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler)
[![simple-component](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component/badge.svg?subject=simple-component)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component)

A minimalistic approach to dependency injection. Basically the idea is that you can do (almost) everything dagger does with only a handful annotations:

1. `@Inject` declares an injection point, similarly `@Provides` declares an injection point within the component
2. `@Qualifier` and `@Named`
3. `@Component` along with `@Component.Factory`

### The `@Scope` and `@Singleton` annotations are ignored.

Instead, you have the "same key, same bean" rule:

> If two beans of the same *key* are injected by the same component, then they are the same bean instance.

Here, "same key" means "same type" or, if a qualifier is used, "same key and same qualifier".
The SKSB-rule may seem restrictive at first, but please note that you can always inject `Provider<TheBean> theBeanProvider`, and calling `theBeanProvider.get()` will give you a freshly wired "distinct" bean instance every time.

### Note to dagger users

There are no "subcomponents" or "component dependencies".
It may not be very elegant, but declaring multiple "normal" components should be "good enough" in most cases.

There is no `@Module`, but you can still have `@Provides` methods, only you declare them directly in your component.
A `@Provides` method must be `static`.

There is no `@Binds`.
It can be emulated with a `@Provides` method, or, if you control the source code of the interface, a static `@Inject` method.

There is no need for the `@BindsInstance` annotation: *every* factory parameter is a bound instance.

There is no `@AssistedInject`, it's a can of worms.

There is no `@IntoList` or `@IntoSet`, you can return these collections from a `@Provides` method.

There is no `Lazy<T>`, please be sure `Provider<T>` does not cover your case before opening an issue.

### Do more with less

* Works with both `javax.inject.Inject` or `jakarta.inject.Inject`.
* Includes its own copy of the JSR-330 annotations, excluding `@Scope` and `@Singleton`, so you don't *have* to depend on one of these.
* Allows injection into static method.
* No typecasts in generated code. Yes, dagger does this, if some of your beans are package-private. Ew, gross!
* Generates only the component implementation and nothing else, so it doesn't bloat your jar as much.

The new feature, "injection into static method" is only allowed if the method's return value matches the type of the enclosing class.
The following is allowed because the method returns `Heater`:

```java
public interface Heater {
    @Inject
    static Heater getInstance() {
        return new ElectricHeater();
    }

    //...
}
```

In the next example, the method `setHeaterFactory` *could* be used to sneak in a mock `Heater` for testing purpose, if invoked early in the test, before the component is created: 

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

By the way you can have more than one "static inject" method in the same class, if you add a qualifier:

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
