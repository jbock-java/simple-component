package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class ProcessorComponentTest {

    @Test
    void staticMethodBindings() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
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
                        "  private final TestClass.A a;",
                        "",
                        "  private TestClass_AComponent_Impl() {",
                        "    TestClass.C c = TestClass.C.createC();",
                        "    TestClass.B b = TestClass.B.createB(c);",
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
    void dependencyDiamond() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
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
                        "final class TestClass_AComponent_Impl implements TestClass.AComponent {",
                        "  private final TestClass.A a;",
                        "",
                        "  private TestClass_AComponent_Impl() {",
                        "    TestClass.E e = new TestClass.E();",
                        "    TestClass.C c = new TestClass.C(e);",
                        "    TestClass.B b = new TestClass.B(e);",
                        "    this.a = new TestClass.A(b, c);",
                        "  }",
                        "",
                        "  static TestClass.AComponent create() {",
                        "    return new TestClass_AComponent_Impl();",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return a;",
                        "  }",
                        "}");
    }

    @Test
    void noRequest() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "",
                "final class TestClass {",
                "",
                "  @Component",
                "  interface AComponent {",
                "  }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.TestClass_AComponent_Impl")
                .containsLines(
                        "package test;",
                        "",
                        "final class TestClass_AComponent_Impl implements TestClass.AComponent {",
                        "  private TestClass_AComponent_Impl() {",
                        "  }",
                        "",
                        "  static TestClass.AComponent create() {",
                        "    return new TestClass_AComponent_Impl();",
                        "  }",
                        "}");
    }

    @Test
    void thoroughTest() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "",
                "class TestClass {",
                "",
                "  @Component interface AComponent {",
                "    A a();",
                "    @Component.Factory interface Factory {",
                "      AComponent create(P p);",
                "    }",
                "  }",
                "",
                "  static class A {",
                "    @Inject A(B b, P p) {}",
                "  }",
                "",
                "  static class B {",
                "    @Inject B(P p, C c) {}",
                "  }",
                "",
                "  static class C {",
                "    @Inject C(P p, D d) {}",
                "  }",
                "",
                "  static class D {",
                "    @Inject D(P p) {}",
                "  }",
                "",
                "  static class P {}",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
    }
}