[![simple-component-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler/badge.svg?color=grey&subject=simple-component-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler)
[![simple-component](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component/badge.svg?subject=simple-component)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component)

### simple-component

A simple dependency injector. 

* Works with both `javax.inject.Inject` or `jakarta.inject.Inject`.
* Also includes its own `@Inject` annotation, for those cases where neither `javax.inject` nor `jakarta.inject` is available.
* Allows static methods as injection sites.
* No typecasts in generated code. Dagger will always generate a typecast when a bound type is not `public`.
* Field injection is not supported.
* Generates only a single class per `@Component`.
* No scoping: For every "key" (typename + optional qualifier), there is always at most one instance per component instance.
* No modules (yet), but you can use `@Component.Factory` to pass parameters to the component.
* No `@Binds` (yet), but you can use injection into static method as described below, to bind a concrete type to an interface. 

Injection into static methods is only allowed if the method's return value matches the type of the enclosing class. For example:

```java
public interface Heater {
    @Inject
    static Heater getInstance() {
        return new ElectricHeater();
    }
}
```

It is possible to cache an instance of `ElectricHeater` across different component instances,
something that cannot be done with dagger.

### Samples

* https://github.com/jbock-java/modular-thermosiphon

### Alternatives

* https://github.com/google/dagger/
* https://github.com/avaje/avaje-inject
