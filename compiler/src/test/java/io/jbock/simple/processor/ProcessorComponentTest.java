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
                "  }",
                "",
                "  @Inject static B createB(C c) { return null; }",
                "",
                "  static interface C {",
                "    @Inject static C createC() { return null; }",
                "  }",
                "",
                "  static interface D {",
                "    @Inject static D createD() { return null; }",
                "  }",
                "",
                "  @Component(mockBuilder = true, publicMockBuilder = true)",
                "  public interface AComponent {",
                "    A getA();",
                "  }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.TestClass_AComponent_Impl")
                .containsLines(
                        "package test;",
                        "",
                        "public final class TestClass_AComponent_Impl implements TestClass.AComponent {",
                        "  private final TestClass.A testClassA;",
                        "",
                        "  private TestClass_AComponent_Impl(TestClass.A testClassA) {",
                        "    this.testClassA = testClassA;",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return testClassA;",
                        "  }",
                        "",
                        "  public static TestClass.AComponent create() {",
                        "    TestClass.C testClassC = TestClass.C.createC();",
                        "    TestClass.B testClassB = TestClass.createB(testClassC);",
                        "    TestClass.A testClassA = new TestClass.A(testClassB);",
                        "    return new TestClass_AComponent_Impl(testClassA);",
                        "  }",
                        "",
                        "  public static MockBuilder mockBuilder() {",
                        "    return new MockBuilder();",
                        "  }",
                        "",
                        "  public static final class MockBuilder {",
                        "    private TestClass.C testClassC;",
                        "    private TestClass.B testClassB;",
                        "    private TestClass.A testClassA;",
                        "    public TestClass.AComponent build() {",
                        "      TestClass.C testClassC = this.testClassC != null ? this.testClassC : TestClass.C.createC();",
                        "      TestClass.B testClassB = this.testClassB != null ? this.testClassB : TestClass.createB(testClassC);",
                        "      TestClass.A testClassA = this.testClassA != null ? this.testClassA : new TestClass.A(testClassB);",
                        "      return new TestClass_AComponent_Impl(testClassA);",
                        "    }",
                        "    public MockBuilder testClassC(TestClass.C testClassC) {",
                        "      this.testClassC = testClassC;",
                        "      return this;",
                        "    }",
                        "    public MockBuilder testClassB(TestClass.B testClassB) {",
                        "      this.testClassB = testClassB;",
                        "      return this;",
                        "    }",
                        "    public MockBuilder testClassA(TestClass.A testClassA) {",
                        "      this.testClassA = testClassA;",
                        "      return this;",
                        "    }",
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
                "  static class B {}",
                "",
                "  @Inject static B createB(E e) { return null; }",
                "",
                "  static class C {",
                "    @Inject C(E e) {}",
                "  }",
                "",
                "  static class E {",
                "    @Inject E() {}",
                "  }",
                "",
                "  @Component(mockBuilder = true)",
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
                        "  private final TestClass.A testClassA;",
                        "",
                        "  private TestClass_AComponent_Impl(TestClass.A testClassA) {",
                        "    this.testClassA = testClassA;",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return testClassA;",
                        "  }",
                        "",
                        "  static TestClass.AComponent create() {",
                        "    TestClass.E testClassE = new TestClass.E();",
                        "    TestClass.C testClassC = new TestClass.C(testClassE);",
                        "    TestClass.B testClassB = TestClass.createB(testClassE);",
                        "    TestClass.A testClassA = new TestClass.A(testClassB, testClassC);",
                        "    return new TestClass_AComponent_Impl(testClassA);",
                        "  }",
                        "",
                        "  static MockBuilder mockBuilder() {",
                        "    return new MockBuilder();",
                        "  }",
                        "",
                        "  static final class MockBuilder {",
                        "    private TestClass.E testClassE;",
                        "    private TestClass.C testClassC;",
                        "    private TestClass.B testClassB;",
                        "    private TestClass.A testClassA;",
                        "",
                        "    TestClass.AComponent build() {",
                        "      TestClass.E testClassE = this.testClassE != null ? this.testClassE : new TestClass.E();",
                        "      TestClass.C testClassC = this.testClassC != null ? this.testClassC : new TestClass.C(testClassE);",
                        "      TestClass.B testClassB = this.testClassB != null ? this.testClassB : TestClass.createB(testClassE);",
                        "      TestClass.A testClassA = this.testClassA != null ? this.testClassA : new TestClass.A(testClassB, testClassC);",
                        "      return new TestClass_AComponent_Impl(testClassA);",
                        "    }",
                        "",
                        "    MockBuilder testClassE(TestClass.E testClassE) {",
                        "      this.testClassE = testClassE;",
                        "      return this;",
                        "    }",
                        "    MockBuilder testClassC(TestClass.C testClassC) {",
                        "      this.testClassC = testClassC;",
                        "      return this;",
                        "    }",
                        "    MockBuilder testClassB(TestClass.B testClassB) {",
                        "      this.testClassB = testClassB;",
                        "      return this;",
                        "    }",
                        "    MockBuilder testClassA(TestClass.A testClassA) {",
                        "      this.testClassA = testClassA;",
                        "      return this;",
                        "    }",
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