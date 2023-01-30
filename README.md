[![simple-component-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler/badge.svg?color=grey&subject=simple-component-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler)
[![simple-component](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component/badge.svg?subject=simple-component)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component)

### simple-component

A minimal dependency injector. Basically the idea is that you can do (almost) everything dagger does with only three annotations:

1. `@Inject`
2. `@Qualifier` along with `@Named`
3. `@Component` along with `@Component.Factory`

Due warning, some dagger features are just not there, in particular `@Scope`.
Instead, you have a guarantee that everything gets created at most once per component instance.
So if you really, *really* need to have multiple distinct "copies" of a particular "bean" in your component, then this might not be for you.

Also subcomponents and "component dependencies" (?) are not there. These are not essential features imho, just a glorified convenience to copy
things from one component to the other.

Finally to be honest there's no `@Module` (yet).
This means you may have to do some manual "init wiring" before you create your component, and pass the results via `@Component.Factory`.

### Do more with less

* Works with both `javax.inject.Inject` or `jakarta.inject.Inject`.
* It also includes its own `@Inject` annotation, so you don't *have* to depend on one of these.
* Allows static methods as injection sites.
* No typecasts in generated code.
* Generates only a single class per `@Component` annotation, so this *should* be faster than dagger and doesn't bloat your jar as much.

The new feature, "injection into static method" is only allowed if the method's return value matches the type of the enclosing class. For example:

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
* [jbock](https://github.com/jbock-java/jbock) uses it

### Alternatives

* https://github.com/google/dagger/
* https://github.com/avaje/avaje-inject
