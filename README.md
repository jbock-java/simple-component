[![simple-component-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler/badge.svg?color=grey&subject=simple-component-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler)
[![simple-component](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component/badge.svg?subject=simple-component)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component)

A minimal dependency injector that uses the following annotations:

1. `@Inject` declares an injection point. It can be a constructor or a static method.
2. `@Provides` declares an injection point within the component. It must be a static method.
3. `@Qualifier` and its default implementation `@Named`.
4. And of course, `@Component`, `@Component.Factory` and `@Component.Builder`.

Note this is not a complete implementation of `javax.inject` or `jakarta.inject`, because:

### The `@Scope` and `@Singleton` annotations are ignored.

Instead, you have the "same key, same bean" rule:

> If two beans of the same *key* are injected by the same component, then they are the same bean instance.

Intuitively this means a component injects the same bean instance everywhere (unless of course you're using qualifiers or inject a provider).

If you want to re-use a bean across multiple components, or multiple instances of the same component, use a `@Factory` or a `@Builder` to pass it.
If possible, the component will use instance that was passed this way, rather than create a new bean instance.

If you inject `Provider<TheBean>`, rather than `TheBean` directly, calling `provider.get()` will create a fresh bean instance every time.

### Mocking

If you want create a component where some beans are swapped for mock instances, use `@Component(mockBuilder = true)`.
The mocks can then be injected into the component using the `mockBuilder` method.
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

There is no `Lazy<T>`, please check if `Provider<T>` covers your use case.

### Samples

* [modular-thermosiphon (dagger's "coffee machine" demo)](https://github.com/jbock-java/modular-thermosiphon)
* [jbock](https://github.com/jbock-java/jbock) uses it, see for example [ValidateComponent](https://github.com/jbock-java/jbock/blob/master/compiler/src/main/java/net/jbock/validate/ValidateComponent.java)

### Alternatives

* https://github.com/google/dagger/
* https://github.com/avaje/avaje-inject
* https://github.com/michaelboyles/simple-di
