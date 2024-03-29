package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class JavaxQualifierTest {

    @Test
    void qualifiedIdentity() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import javax.inject.Inject;",
                "import javax.inject.Named;",
                "",
                "final class TestClass {",
                "",
                "  @Component(mockBuilder = true)",
                "  interface AComponent {",
                "    @Named(\"a\") String getS();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create(@Named(\"a\") String a, @Named(\"b\") String b);",
                "    }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.TestClass_AComponent_Impl")
                .containsLines(
                        "package test;",
                        "",
                        "final class TestClass_AComponent_Impl implements TestClass.AComponent {",
                        "  private final String a;",
                        "",
                        "  private TestClass_AComponent_Impl(String a) {",
                        "    this.a = a;",
                        "  }",
                        "",
                        "  @Override",
                        "  public String getS() {",
                        "    return a;",
                        "  }",
                        "",
                        "  static TestClass.AComponent.Factory factory() {",
                        "    return new Factory_Impl();",
                        "  }",
                        "",
                        "  private static final class Factory_Impl implements TestClass.AComponent.Factory {",
                        "    @Override",
                        "    public TestClass.AComponent create(String a, String b) {",
                        "      return new TestClass_AComponent_Impl(a);",
                        "    }",
                        "  }",
                        "}");
    }
}