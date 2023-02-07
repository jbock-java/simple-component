[![simple-component-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler/badge.svg?color=grey&subject=simple-component-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler)
[![simple-component](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component/badge.svg?subject=simple-component)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component)

A minimalistic approach to dependency injection. Basically the idea is that you can do (almost) everything dagger does with only a handful annotations:

1. `@Inject` declares an injection point, similarly `@Provides` declares an injection point within the component
2. `@Qualifier` and `@Named`
3. `@Component` along with `@Component.Factory`

### Note to `javax.inject` users

The `@Scope` and `@Singleton` annotations are **ignored!**
They are too confusing.
Instead, you have a simple rule: Everywhere a particular bean is injected, you get *the same instance* of the bean.

But of course, if you *need* to have multiple (distinct) instances of a bean, you can inject a `Provider<TheBean> beanProvider`.
It will create a new bean instance everytime `beanProvider.get()` is invoked.

### Note to dagger users

There are no "subcomponents" or "component dependencies".
It may not be very elegant, but declaring several regular components should be enough in most cases.

There is no `@Module`; instead, you can have `@Provides` methods directly in the component.
A `@Provides` method must be `static`.

There is no `@Binds`.
It can be emulated with a `@Provides` method, or, if you control the source code of the interface, a static `@Inject` method.

There is no need for the `@BindsInstance` annotation.

Please note, unlike in dagger, there is no way to associate a particular bean with a particular component.
A component implementation may use *any* `@Inject` - annotated constructor (or static method), as long as it is accessible to it, by Java's normal visibility rules.

### Do more with less

* Works with both `javax.inject.Inject` or `jakarta.inject.Inject`.
* Includes its own copy of the JSR-330 annotations, excluding `@Scope` and `@Singleton`, so you don't *have* to depend on one of these.
* Allows injection into static method.
* No typecasts in generated code. Yes, dagger may generate unnecessary typecasts if some of your beans are package-private, even if they are in the same package as the component.
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
