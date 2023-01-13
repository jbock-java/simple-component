package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class ProcessorComponentTest {

    @Test
    void simpleComponent() {
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
                "  static class B {}",
                "  static class C {}",
                "  static class D {}",
                "",
                "  @Inject",
                "  static B createB(C c) { return null; }",
                "",
                "  @Inject",
                "  static C createC() { return null; }",
                "",
                "  @Inject",
                "  static D createD() { return null; }",
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
                        "class TestClass_AComponent_Impl implements TestClass.AComponent {",
                        "  private final TestClass.C c;",
                        "  private final TestClass.B b;",
                        "  private final TestClass.A a;",
                        "",
                        "  private TestClass_AComponent_Impl() {",
                        "    this.c = TestClass.createC();",
                        "    this.b = TestClass.createB(c);",
                        "    this.a = new TestClass.A(b);",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return a;",
                        "  }",
                        "}");
    }

    @Test
    void simpleDiamond() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import jakarta.inject.Inject;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(B b, C c) {}",
                "  }",
                "",
                "  static class B {",
                "    @Inject B(E e) {}",
                "  }",
                "",
                "  static class C {",
                "    @Inject C(E e) {}",
                "  }",
                "",
                "  static class E {",
                "    @Inject E() {}",
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
                        "class TestClass_AComponent_Impl implements TestClass.AComponent {",
                        "  private final TestClass.E e;",
                        "  private final TestClass.C c;",
                        "  private final TestClass.B b;",
                        "  private final TestClass.A a;",
                        "",
                        "  private TestClass_AComponent_Impl() {",
                        "    this.e = new TestClass.E();",
                        "    this.c = new TestClass.C(e);",
                        "    this.b = new TestClass.B(e);",
                        "    this.a = new TestClass.A(b, c);",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return a;",
                        "  }",
                        "}");
    }
}