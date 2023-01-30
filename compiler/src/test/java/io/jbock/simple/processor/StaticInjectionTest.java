package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class StaticInjectionTest {

    @Test
    void clashResolvedByQualifiers() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Named;",
                "",
                "final class TestClass {",
                "",
                "  static class A {",
                "    @Inject A(@Named(\"1\") B b1, @Named(\"2\") B b2) {}",
                "  }",
                "",
                "  static class B {",
                "    @Inject @Named(\"1\") static B create1() { return null; }",
                "    @Inject @Named(\"2\") static B create2() { return null; }",
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
                        "    TestClass.B b = TestClass.B.create1();",
                        "    TestClass.B b2 = TestClass.B.create2();",
                        "    this.a = new TestClass.A(b, b2);",
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
}
