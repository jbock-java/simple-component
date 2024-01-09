[![simple-component-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler/badge.svg?color=grey&subject=simple-component-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler)
[![simple-component](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component/badge.svg?subject=simple-component)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component)

tl;dr minimal example:

```java
class CoffeeApp {

    @Component interface CoffeeComponent {
        CoffeeMaker coffeeMaker();

        @Component.Builder interface Builder {
            Builder logLevel(String logLevel);
            CoffeeComponent buildComponent();
        }
    }

    interface Logger {
        void log(String msg);
    }

    static class CoffeeMaker {
        private final Logger logger;

        @Inject CoffeeMaker(Logger logger) {
            this.logger = logger;
        }

        void brew() {
            logger.log("~ ~ ~ heating ~ ~ ~");
            logger.log("=> => pumping => =>");
            logger.log(" [_]P coffee! [_]P ");
        }
    }

    @Inject static Logger createLogger(String level) {
        return msg -> System.out.println(level + " " + msg);
    }
}
```

This dependency injector uses the following annotations:
1. `@Inject` declares an injection point. It can be a constructor or a static method in the bean class. It can also be a static method in the component class.
2. `@Qualifier` and its default implementation `@Named`.
3. And of course, `@Component`, `@Component.Factory` and `@Component.Builder`.

Note this is not a complete implementation of `javax.inject` or `jakarta.inject`, because:

### The `@Scope` and `@Singleton` annotations are ignored.

Instead there's the following rule:

> If two beans of the *same type* and *same qualifier* are injected by the *same component*, then they are the *same instance*.

Intuitively this means the same bean instance is injected everywhere (unless you're using qualifiers, or inject a provider).
In the example above, if multiple beans would request the logger, they would all get the same logger instance.

If you want to re-use a bean instance across multiple components, or multiple instances of the same component, use a `@Factory` or a `@Builder` to pass it around.
Components will prefer using an existing bean instance over creating a new one.

If you inject `Provider<TheBean>`, rather than `TheBean` directly, calling `provider.get()` will create a fresh bean instance every time.

### Mocking

If you want create a component where some beans are swapped for mock instances, use `@Component(mockBuilder = true)`.
A static `mockBuilder` method will be generated, which returns a MockBuilder that can be used to register your mocks.
(If your component uses `@Component.Builder`, the generated builder will have a `withMocks` method that returns the MockBuilder.)
[Usage example](https://github.com/jbock-java/modular-thermosiphon):

```java
List<String> messages = new ArrayList<>();
CoffeeApp.Logger mockLogger = messages::add;
CoffeeApp.CoffeeComponent app = CoffeeApp_CoffeeComponent_Impl.builder()
        .logLevel("")
        .withMocks()
        .coffeeAppLogger(mockLogger)
        .build();
app.coffeeMaker().brew();
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
