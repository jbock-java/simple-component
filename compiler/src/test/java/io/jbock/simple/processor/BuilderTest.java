package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class BuilderTest {

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
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    @Component.Builder",
                "    interface Builder {",
                "      AComponent build();",
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
                        "  private TestClass_AComponent_Impl() {",
                        "    this.a = new TestClass.A();",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return a;",
                        "  }",
                        "",
                        "  static TestClass.AComponent.Builder builder() {",
                        "    return new Builder_Impl();",
                        "  }",
                        "",
                        "  private static final class Builder_Impl implements TestClass.AComponent.Builder {",
                        "    @Override",
                        "    public TestClass.AComponent build() {",
                        "      return new TestClass_AComponent_Impl();",
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
                "  @Component",
                "  interface AComponent {",
                "    String getS();",
                "",
                "    @Component.Builder",
                "    interface Builder {",
                "      Builder withS(String s);",
                "      AComponent build();",
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
                        "  static TestClass.AComponent.Builder builder() {",
                        "    return new Builder_Impl();",
                        "  }",
                        "",
                        "  private static final class Builder_Impl implements TestClass.AComponent.Builder {",
                        "    String s;",
                        "",
                        "    @Override",
                        "    public TestClass.AComponent.Builder withS(String s) {",
                        "      this.s = s;",
                        "      return this;",
                        "    }",
                        "",
                        "    @Override",
                        "    public TestClass.AComponent build() {",
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
                "import io.jbock.simple.Named;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(@Named(\"a\") String s) {}",
                "  }",
                "",
                "  @Component",
                "  public interface AComponent {",
                "    A getA();",
                "",
                "    @Component.Builder",
                "    interface Builder {",
                "      Builder withS(@Named(\"a\") String s);",
                "      AComponent build();",
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
                        "  private final TestClass.A a;",
                        "",
                        "  private TestClass_AComponent_Impl(String s) {",
                        "    this.a = new TestClass.A(s);",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return a;",
                        "  }",
                        "",
                        "  public static TestClass.AComponent.Builder builder() {",
                        "    return new Builder_Impl();",
                        "  }",
                        "",
                        "  private static final class Builder_Impl implements TestClass.AComponent.Builder {",
                        "    String s;",
                        "",
                        "    @Override",
                        "    public TestClass.AComponent.Builder withS(String s) {",
                        "      this.s = s;",
                        "      return this;",
                        "    }",
                        "",
                        "    @Override",
                        "    public TestClass.AComponent build() {",
                        "      return new TestClass_AComponent_Impl(s);",
                        "    }",
                        "  }",
                        "}");
    }
}