package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class ProviderTest {

    @Test
    void qualifierFail() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Provider;",
                "import io.jbock.simple.Named;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(@Named(\"b\") Provider<B> bProvider) {}",
                "  }",
                "",
                "  static class B {",
                "    static @Inject B createB() { return null; }",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "  }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Binding not found");
    }

    @Test
    void qualifierSuccess() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Provider;",
                "import io.jbock.simple.Named;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(@Named(\"b\") Provider<B> bProvider, @Named(\"b\") B b) {}",
                "  }",
                "",
                "  static class B {",
                "    static @Named(\"b\") @Inject B createB() { return null; }",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "  }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.TestClass_AComponent_Impl")
                .containsLines(
                        "package test;",
                        "",
                        "final class TestClass_AComponent_Impl implements TestClass.AComponent {",
                        "  private final TestClass.A a;",
                        "",
                        "  private TestClass_AComponent_Impl() {",
                        "    Provider<TestClass.B> bProvider = () -> TestClass.B.createB();",
                        "    TestClass.B b = TestClass.B.createB();",
                        "    this.a = new TestClass.A(bProvider, b);",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return a;",
                        "  }",
                        "}");
    }
}