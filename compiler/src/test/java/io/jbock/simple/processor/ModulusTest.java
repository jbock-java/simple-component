package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class ModulusTest {

    @Test
    void clashResolvedByQualifiers() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Provides;",
                "import io.jbock.simple.Named;",
                "import io.jbock.simple.Modulus;",
                "",
                "final class TestClass {",
                "",
                "  static class A {",
                "    @Inject A() {}",
                "  }",
                "",
                "  static class M {",
                "  }",
                "",
                "  @Component(modules = M.class)",
                "  interface AComponent {",
                "    A getA();",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("The module must be annotated with @Modulus");
    }
}
