package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class BindingValidationTest {

    @Test
    void multipleStaticBindings() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Provides;",
                "",
                "final class TestClass {",
                "",
                "  @Provides static TestClass createTestClass1() { return null; }",
                "  @Provides static TestClass createTestClass2() { return null; }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("The @Provides method must be nested inside a @Component or @Modulus");
    }
}
