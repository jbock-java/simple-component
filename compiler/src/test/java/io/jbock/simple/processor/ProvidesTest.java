package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class ProvidesTest {

    @Test
    void providesString() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Provides;",
                "",
                "final class TestClass {",
                "  @Inject",
                "  static class A {",
                "    A(String s) {}",
                "  }",
                "",
                "  @Inject",
                "  static class B {",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    @Provides static String getString(B b) { return null; }",
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
    void providesFunction() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Provides;",
                "import java.util.function.Function;",
                "import java.util.Map;",
                "",
                "final class TestClass {",
                "  @Inject",
                "  record A(Map<String, Function<String, Integer>> f) {",
                "  }",
                "",
                "  @Inject",
                "  static class B {",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    @Provides static Map<String, Function<String, Integer>> provideMyMap(B b) { return null; }",
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
    void nonstaticProvidesMethod() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import io.jbock.simple.Provides;",
                "",
                "final class TestClass {",
                "  @Inject",
                "  record A(String s) {",
                "  }",
                "",
                "  @Inject",
                "  static class B {",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    @Provides default String getString() { return null; }",
                "  }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("The @Provides method must be static");
    }

    @Test
    void simpleProvides() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Provides;",
                "",
                "@Component",
                "interface TestClass {",
                "    int getInt();",
                "    @Provides static int provideInt() { return 5; }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
    }
}
