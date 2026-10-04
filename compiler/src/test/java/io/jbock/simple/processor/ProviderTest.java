package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class ProviderTest {

    @Test
    void qualifierFail() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import java.util.function.Supplier;",
                "import io.jbock.simple.Named;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(@Named(\"b\") Supplier<B> bProvider) {}",
                "  }",
                "",
                "  static class B {",
                "    @Inject B() {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "  }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("No binding found for java.util.function.Supplier<test.TestClass.B> with qualifier @Named(\"b\").")
                .inFile(component)
                .onLineContaining("interface AComponent");
    }

    @Test
    void qualifierSuccess() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import java.util.function.Supplier;",
                "import io.jbock.simple.Provides;",
                "import io.jbock.simple.Named;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(@Named(\"b\") B bProvider, @Named(\"b\") B b) {}",
                "  }",
                "",
                "  static class B {",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    static @Named(\"b\") @Provides B createB() { return null; }",
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
                        "}");
    }

    @Test
    void providedParameter() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Provides;",
                "import io.jbock.simple.Named;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(B bProvider, @Named(\"b\") B b) {}",
                "  }",
                "",
                "  static class B {",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    static @Named(\"b\") @Provides B createB() { return null; }",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      AComponent create(B b);",
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
                        "}");
    }

    @Test
    void simpleProvider() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Provides;",
                "import java.util.function.Supplier;",
                "",
                "@Component",
                "interface TestClass {",
                "  Supplier<Integer> getNumber();",
                "  @Provides static Integer provideNumber() { return 5; }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed(); // supplier not supported
    }

    @Test
    void simpleFactoryProvider() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Provides;",
                "import java.util.function.Supplier;",
                "",
                "@Component",
                "interface TestClass {",
                "  Supplier<Integer> getNumber();",
                "  @Component.Factory",
                "  interface Factory {",
                "    TestClass create(B b);",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed(); // supplier not supported
    }
}
