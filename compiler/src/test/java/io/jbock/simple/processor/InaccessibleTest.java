package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class InaccessibleTest {

    @Test
    void inaccessible() {
        JavaFileObject classB = forSourceLines("test.sub.B",
                "package test.sub;",
                "",
                "import io.jbock.simple.Inject;",
                "",
                "public final class B {",
                "  @Inject B() {}",
                "}");
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Provider;",
                "import test.sub.B;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(Provider<B> b) {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(classB, component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("constructor is not accessible");
    }
}