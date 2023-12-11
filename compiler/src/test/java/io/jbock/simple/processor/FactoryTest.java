package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class FactoryTest {

    @Test
    void noParameters() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A() {}",
                "  }",
                "",
                "  @Component(mockBuilder = true)",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create();",
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
                        "  private final TestClass.A a;",
                        "",
                        "  private TestClass_AComponent_Impl(TestClass.A a) {",
                        "    this.a = a;",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return a;",
                        "  }",
                        "",
                        "  static Factory_Impl factory() {",
                        "    return new Factory_Impl();",
                        "  }",
                        "",
                        "  private static final class Factory_Impl implements TestClass.AComponent.Factory {",
                        "    @Override",
                        "    public TestClass.AComponent create() {",
                        "      TestClass.A a = new TestClass.A();",
                        "      return new TestClass_AComponent_Impl(a);",
                        "    }",
                        "  }",
                        "}");
    }

    @Test
    void factoryParameterIdentity() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "",
                "final class TestClass {",
                "",
                "  @Component(mockBuilder = true)",
                "  public interface AComponent {",
                "    String getS();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create(String s);",
                "    }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.TestClass_AComponent_Impl")
                .containsLines(
                        "package test;",
                        "",
                        "public final class TestClass_AComponent_Impl implements TestClass.AComponent {",
                        "  private final String s;",
                        "",
                        "  private TestClass_AComponent_Impl(String s) {",
                        "    this.s = s;",
                        "  }",
                        "",
                        "  @Override",
                        "  public String getS() {",
                        "    return s;",
                        "  }",
                        "",
                        "  public static Factory_Impl factory() {",
                        "    return new Factory_Impl();",
                        "  }",
                        "",
                        "  private static final class Factory_Impl implements TestClass.AComponent.Factory {",
                        "    @Override",
                        "    public TestClass.AComponent create(String s) {",
                        "      return new TestClass_AComponent_Impl(s);",
                        "    }",
                        "  }",
                        "}");
    }


    @Test
    void factoryParameter() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(String s) {}",
                "  }",
                "",
                "  @Component(mockBuilder = true)",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create(String s);",
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
                        "  private final TestClass.A a;",
                        "",
                        "  private TestClass_AComponent_Impl(TestClass.A a) {",
                        "    this.a = a;",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return a;",
                        "  }",
                        "",
                        "  static Factory_Impl factory() {",
                        "    return new Factory_Impl();",
                        "  }",
                        "",
                        "  private static final class Factory_Impl implements TestClass.AComponent.Factory {",
                        "    @Override",
                        "    public TestClass.AComponent create(String s) {",
                        "      TestClass.A a = new TestClass.A(s);",
                        "      return new TestClass_AComponent_Impl(a);",
                        "    }",
                        "  }",
                        "}");
    }
}