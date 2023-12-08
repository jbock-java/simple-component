[![simple-component-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler/badge.svg?color=grey&subject=simple-component-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler)
[![simple-component](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component/badge.svg?subject=simple-component)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component)

A minimal dependency injector that uses the following annotations:

1. `@Inject` declares an injection point. It can be a constructor or a static method.
2. `@Provides` declares an injection point within the component. It must be a static method.
3. `@Qualifier` and its default implementation `@Named`.
4. And of course, `@Component`, `@Component.Factory` and `@Component.Builder`.

### The `@Scope` and `@Singleton` annotations are ignored.

Instead, you have the "same key, same bean" rule:

> If two beans of the same *key* are injected by the same component, then they are the same bean instance.

This means a component contains at most one instance per bean class (unless of course you're using qualifiers).

If you want to re-use a bean across multiple components, use a `@Factory` or a `@Builder` to pass it.
If possible, the component will use instance that was passed this way, rather than create a new bean instance.

If you inject `Provider<TheBean>`, rather than `TheBean` directly, calling `provider.get()` will create a fresh bean instance every time.

### Mocking

If you want create a component where some means are swapped for mock instance, use `@Component(mockBuilder = true)`.
The mocks can then be injected in to the component using the `mockBuilder` method.
For [example](https://github.com/jbock-java/modular-thermosiphon):

```java
List<String> messages = new ArrayList<>();
CoffeeLogger mockLogger = new CoffeeLogger("") {
    @Override
    public void log(String msg) {
        messages.add(msg);
    }
};
CoffeeApp.CoffeeShop app = CoffeeApp_CoffeeShop_Impl.mockBuilder()
        .coffeeLogger(mockLogger)
        .build()
        .create("");
app.maker().brew();
assertEquals(List.of(
                "~ ~ ~ heating ~ ~ ~",
                "=> => pumping => =>",
                " [_]P coffee! [_]P "),
        messages);
```

### Note to dagger users

There are no "subcomponents" or "component dependencies".

There is no `@Module`, but you can still have `@Provides` methods, only you declare them directly in your component.
A `@Provides` method must be `static`.

There is no `@Binds`.
It can be emulated with a `@Provides` method, or, if you control the source code of the interface, a static `@Inject` method.

There is no need for the `@BindsInstance` annotation. Every factory parameter or builder parameter is a bound instance.

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
