package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class QualifierTest {

    @Test
    void qualifiedParameters() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import jakarta.inject.Inject;",
                "import jakarta.inject.Named;",
                "",
                "final class TestClass {",
                "",
                "  static class A {",
                "    @Inject A(@Named(\"a\") String a) {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create(@Named(\"a\") String a, @Named(\"b\") String b);",
                "    }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
    }

    @Test
    void qualifiedInjectionSites() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import jakarta.inject.Inject;",
                "import jakarta.inject.Named;",
                "",
                "final class TestClass {",
                "",
                "  @Inject @Named(\"a\") static String createString1() { return null; }",
                "  @Inject @Named(\"b\") static String createString2() { return null; }",
                "",
                "  static class A {",
                "    @Inject A(@Named(\"a\") String s) {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
    }

    @Test
    void qualifiedIdentity() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import jakarta.inject.Inject;",
                "import jakarta.inject.Named;",
                "",
                "final class TestClass {",
                "",
                "  @Component",
                "  interface AComponent {",
                "    @Named(\"a\") String getS();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create(@Named(\"a\") String a, @Named(\"b\") String b);",
                "    }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.TestClass_AComponent_Impl")
                .containsLines(
                        "package test;",
                        "",
                        "class TestClass_AComponent_Impl implements TestClass.AComponent {",
                        "  private final String a;",
                        "  private final String b;",
                        "",
                        "  private TestClass_AComponent_Impl(String a, String b) {",
                        "    this.a = a;",
                        "    this.b = b;",
                        "  }",
                        "",
                        "  @Override",
                        "  public String getS() {",
                        "    return a;",
                        "  }",
                        "",
                        "  TestClass.AComponent.Factory factory() {",
                        "    return new Factory_Impl();",
                        "  }",
                        "",
                        "  class Factory_Impl implements TestClass.AComponent.Factory {",
                        "    @Override",
                        "    public TestClass.AComponent create(String a, String b) {",
                        "      return new TestClass_AComponent_Impl(a, b);",
                        "    }",
                        "  }",
                        "}");
    }
}