package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class PrimitiveTest {

    @Test
    void primitiveParameter() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Named;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(@Named(\"a\") int i) {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create(@Named(\"a\") int i);",
                "    }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.TestClass_AComponent_Impl")
                .containsLines(
                        "  private TestClass_AComponent_Impl(int i) {",
                        "    this.a = new TestClass.A(i);",
                        "  }");
    }

    @Test
    void providesPrimitive() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Named;",
                "import io.jbock.simple.Provides;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(@Named(\"b\") int i) {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create(@Named(\"a\") int i);",
                "    }",
                "",
                "    @Named(\"b\")",
                "    @Provides",
                "    static int getB(@Named(\"a\") int i) { return i; }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.TestClass_AComponent_Impl")
                .containsLines(
                        "  private TestClass_AComponent_Impl(int i) {",
                        "    int i2 = TestClass.AComponent.getB(i);",
                        "    this.a = new TestClass.A(i2);",
                        "  }");
    }
}
