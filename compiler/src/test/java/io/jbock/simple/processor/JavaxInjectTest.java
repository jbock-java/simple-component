package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class JavaxInjectTest {

    @Test
    void staticMethodBindings() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import javax.inject.Inject;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(B b) {}",
                "  }",
                "",
                "  static interface B {",
                "    @Inject static B createB(C c) { return null; }",
                "  }",
                "",
                "  static interface C {",
                "    @Inject static C createC() { return null; }",
                "  }",
                "",
                "  static interface D {",
                "    @Inject static D createD() { return null; }",
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
                        "  private final TestClass.C c;",
                        "  private final TestClass.B b;",
                        "  private final TestClass.A a;",
                        "",
                        "  private TestClass_AComponent_Impl() {",
                        "    this.c = TestClass.C.createC();",
                        "    this.b = TestClass.B.createB(c);",
                        "    this.a = new TestClass.A(b);",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return a;",
                        "  }",
                        "}");
    }
}
