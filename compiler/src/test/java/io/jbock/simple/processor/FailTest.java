package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class FailTest {

    @Disabled
    @Test
    void twoConstructors() {
        JavaFileObject baka = forSourceLines("test.Baka",
                "package test;",
                "import io.jbock.simple.Inject;",
                "",
                "@Inject",
                "class Baka {",
                "  Baka() {}",
                "  Baka(String s) {}",
                "}");
        Compilation compilation = simpleCompiler().compile(baka, bakaTestComponent());
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("more than one constructor found");
    }

    @Disabled
    @Test
    void checkedException() {
        JavaFileObject baka = forSourceLines("test.Baka",
                "package test;",
                "import io.jbock.simple.Inject;",
                "",
                "@Inject",
                "class Baka {",
                "  Baka() throws java.io.IOException {}",
                "}");
        Compilation compilation = simpleCompiler().compile(baka, bakaTestComponent());
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Checked exception is not allowed");
    }

    static JavaFileObject bakaTestComponent() {
        return forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Provides;",
                "",
                "final class TestClass {",
                "",
                "  @Component",
                "  interface AComponent {",
                "    Baka getBaka();",
                "  }",
                "}");
    }
}
