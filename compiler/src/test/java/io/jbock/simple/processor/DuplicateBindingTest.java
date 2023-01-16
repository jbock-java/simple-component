package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class DuplicateBindingTest {

    @Test
    void duplicateParameterBinding() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import jakarta.inject.Inject;",
                "",
                "final class TestClass {",
                "",
                "  @Component",
                "  interface AComponent {",
                "    String getS();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create(String a, String b);",
                "    }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("The binding is in conflict with another parameter: java.lang.String a");
    }

    @Test
    void duplicateBinding() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import jakarta.inject.Inject;",
                "",
                "final class TestClass {",
                "",
                "  @Inject static String createString1() { return null; }",
                "  @Inject static String createString2() { return null; }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    String getString();",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("There is a conflicting binding: test.TestClass.createString");
    }
}