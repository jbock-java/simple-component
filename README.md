[![simple-component-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler/badge.svg?color=grey&subject=simple-component-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler)
[![simple-component](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component/badge.svg?subject=simple-component)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component)

### simple-component

A minimalistic approach to dependency injection. Basically the idea is that you can do (almost) everything dagger does with only a handful annotations:

1. `@Inject` declares an injection site, `@Provides` declares an injection site within the component
2. `@Qualifier` and `@Named`
3. `@Component` along with `@Component.Factory`

There is no `@Scope`. Instead, you have a guarantee that every injection site gets called at most once per component instance.
If you need multiple distinct "copies" of a particular "bean" in your component, then only dagger can help you.
In my experience this is a rare case and should not be the default behaviour, like it is in dagger.
It can also be worked around by injecting a hand-rolled "BeanFactory" instead.

There are no subcomponents or "component dependencies".

There is no `@Module`, but there is still `@Provides`.
The provides methods must be static and live directly in the component.

There is no `@Binds`, use a "static method binding" instead.

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

You can have more than one "static injection site" if you add a qualifier:

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
