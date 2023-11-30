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
                        "  private TestClass_AComponent_Impl(TestClass.A a) {",
                        "    this.a = a;",
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
                "  public interface AComponent {",
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
                        "  private TestClass_AComponent_Impl(TestClass.A a) {",
                        "    this.a = a;",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return a;",
                        "  }",
                        "",
                        "  public static TestClass.AComponent.Factory factory() {",
                        "    return new Factory_Impl(null);",
                        "  }",
                        "",
                        "  private static final class Factory_Impl implements TestClass.AComponent.Factory {",
                        "    @Override",
                        "    public TestClass.AComponent create(int i) {",
                        "      int b = this.mockBuilder != null && this.mockBuilder.b_isSet ? this.mockBuilder.b : TestClass.AComponent.getB(i);",
                        "      TestClass.A a = this.mockBuilder != null && this.mockBuilder.a != null ? this.mockBuilder.a : new TestClass.A(b);",
                        "      return new TestClass_AComponent_Impl(a);",
                        "    }",
                        "  }",
                        "",
                        "  public static final class MockBuilder {",
                        "    private int b;",
                        "    private boolean b_isSet;",
                        "    private TestClass.A a;",
                        "",
                        "    public TestClass.AComponent.Factory build() {",
                        "      return new Factory_Impl(this);",
                        "    }",
                        "",
                        "    public void b(int b) {",
                        "      this.b = b;",
                        "      this.b_isSet = true;",
                        "    }",
                        "",
                        "    public void a(TestClass.A a) {",
                        "      this.a = a;",
                        "    }",
                        "  }",
                        "}");
    }
}
