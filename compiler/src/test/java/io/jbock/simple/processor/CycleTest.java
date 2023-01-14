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
                "import jakarta.inject.Inject;",
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
                "  static class C {",
                "    @Inject C(D d) {}",
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
                "        test.TestClass.D(b)",
                "    test.TestClass.D is injected at",
                "        test.TestClass.C(d)",
                "    test.TestClass.C is injected at",
                "        test.TestClass.B(c)"));
    }
}