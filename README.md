[![simple-component-compiler](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler/badge.svg?color=grey&subject=simple-component-compiler)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component-compiler)
[![simple-component](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component/badge.svg?subject=simple-component)](https://maven-badges.herokuapp.com/maven-central/io.github.jbock-java/simple-component)

### simple-component

A simple dependency injector. 

* Works with both `javax.inject.Inject` or `jakarta.inject.Inject`.
* Also includes its own `@Inject` annotation, for those cases where neither `javax.inject` nor `jakarta.inject` is available.
* Allows static methods as injection sites.
* No typecasts in generated code.
* Field injection is not supported.
* Generates only a single class per `@Component`.
* No scoping: For every "key" (typename + optional qualifier), there is always at most one instance per component instance.

### Samples

* https://github.com/jbock-java/modular-thermosiphon
