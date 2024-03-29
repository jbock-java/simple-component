package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.simple.processor.TestUtils.message;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class CycleTest {

    @Test
    void basicCycle() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(B b) {}",
                "  }",
                "",
                "  static class B {",
                "    @Inject B(C c) {}",
                "  }",
                "",
                "  interface C {",
                "    @Inject static C createC(D d) { return null; }",
                "  }",
                "",
                "  static class D {",
                "    @Inject D(B b) {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "  }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(message(
                "Found a dependency cycle:",
                "    test.TestClass.B is injected at",
                "        D(test.TestClass.B)",
                "    test.TestClass.D is injected at",
                "        C.createC(test.TestClass.D)",
                "    test.TestClass.C is injected at",
                "        B(test.TestClass.C)"));
    }

    @Test
    void providerCycle() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Provider;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(B b) {}",
                "  }",
                "",
                "  static class B {",
                "    @Inject B(C c) {}",
                "  }",
                "",
                "  interface C {",
                "    @Inject static C createC(D d) { return null; }",
                "  }",
                "",
                "  static class D {",
                "    @Inject D(Provider<B> bProvider) {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "  }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(message(
                "Found a dependency cycle:",
                "    test.TestClass.C is injected at",
                "        B(test.TestClass.C)",
                "    io.jbock.simple.Provider<test.TestClass.B> is injected at",
                "        D(io.jbock.simple.Provider<test.TestClass.B>)",
                "    test.TestClass.D is injected at",
                "        C.createC(test.TestClass.D)"));
    }

    @Test
    void providerProvider() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Provider;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(B b) {}",
                "  }",
                "",
                "  static class B {",
                "    @Inject B(C c) {}",
                "  }",
                "",
                "  interface C {",
                "    @Inject static C createC(D d) { return null; }",
                "  }",
                "",
                "  static class D {",
                "    @Inject D(Provider<Provider<B>> bProvider) {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "  }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("No binding found for io.jbock.simple.Provider<io.jbock.simple.Provider<test.TestClass.B>>");
    }
}