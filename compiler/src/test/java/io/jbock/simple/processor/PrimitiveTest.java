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
                        "  private TestClass_AComponent_Impl(TestClass.A testClassA) {",
                        "    this.testClassA = testClassA;",
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
                "    @Inject A(@Named(\"miku\") int i) {}",
                "  }",
                "",
                "  @Component(mockBuilder = true)",
                "  public interface AComponent {",
                "    A getA();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create(@Named(\"a\") int i);",
                "    }",
                "",
                "    @Named(\"miku\")",
                "    @Provides",
                "    static int getB(@Named(\"a\") int i) { return i; }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.TestClass_AComponent_Impl")
                .containsLines(
                        "  private TestClass_AComponent_Impl(TestClass.A testClassA) {",
                        "    this.testClassA = testClassA;",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return testClassA;",
                        "  }",
                        "",
                        "  public static TestClass.AComponent.Factory factory() {",
                        "    return new Factory_Impl();",
                        "  }",
                        "",
                        "  private static final class Factory_Impl implements TestClass.AComponent.Factory {",
                        "    @Override",
                        "    public TestClass.AComponent create(int i) {",
                        "      int aComponentInt = TestClass.AComponent.getB(i);",
                        "      TestClass.A testClassA = new TestClass.A(aComponentInt);",
                        "      return new TestClass_AComponent_Impl(testClassA);",
                        "    }",
                        "  }",
                        "",
                        "  public static final class MockBuilder {",
                        "    private final int i;",
                        "    private int aComponentInt;",
                        "    private boolean aComponentInt_isSet;",
                        "    private TestClass.A testClassA;",
                        "",
                        "    public TestClass.AComponent build() {",
                        "      int aComponentInt = this.aComponentInt_isSet ? this.aComponentInt : TestClass.AComponent.getB(this.i);",
                        "      TestClass.A testClassA = this.testClassA != null ? this.testClassA : new TestClass.A(aComponentInt);",
                        "      return new TestClass_AComponent_Impl(testClassA);",
                        "    }",
                        "",
                        "    public MockBuilder aComponentInt(int aComponentInt) {",
                        "      this.aComponentInt = aComponentInt;",
                        "      this.aComponentInt_isSet = true;",
                        "      return this;",
                        "    }",
                        "",
                        "    public MockBuilder testClassA(TestClass.A testClassA) {",
                        "      this.testClassA = testClassA;",
                        "      return this;",
                        "    }",
                        "  }",
                        "}");
    }
}
