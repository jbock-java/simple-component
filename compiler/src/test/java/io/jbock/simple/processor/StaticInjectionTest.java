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
                "import io.jbock.simple.Provides;",
                "import io.jbock.simple.Named;",
                "import io.jbock.simple.Modulus;",
                "",
                "final class TestClass {",
                "",
                "  static class A {",
                "    @Inject A(@Named(\"1\") B b1, @Named(\"2\") B b2) {}",
                "  }",
                "",
                "  static class B {",
                "  }",
                "",
                "  @Modulus",
                "  static class M {",
                "  }",
                "",
                "  @Component(modules = M.class)",
                "  interface AComponent {",
                "    A getA();",
                "    @Provides @Named(\"1\") static B create1(String s) { return null; }",
                "    @Provides @Named(\"2\") static B create2() { return null; }",
                "    @Provides static String createString() { return \"\"; }",
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
                        "    String aComponentString = TestClass.AComponent.createString();",
                        "    TestClass.B aComponentB = TestClass.AComponent.create1(aComponentString);",
                        "    TestClass.B aComponentB2 = TestClass.AComponent.create2();",
                        "    TestClass.A testClassA = new TestClass.A(aComponentB, aComponentB2);",
                        "    return new TestClass_AComponent_Impl(testClassA);",
                        "  }",
                        "}");
    }

    @Test
    void injectMethodIsSibling() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Provides;",
                "import io.jbock.simple.Named;",
                "",
                "final class TestClass {",
                "",
                "  static class A {",
                "  }",
                "",
                "  static class B {",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "",
                "    A getA();",
                "",
                "    @Provides @Named(\"1\") static B createB1() { return null; }",
                "    @Provides @Named(\"2\") static B createB2() { return null; }",
                "    @Provides static A createA(@Named(\"1\") B b1, @Named(\"2\") B b2) { return null; }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.TestClass_AComponent_Impl")
                .containsLines(
                        "package test;",
                        "",
                        "final class TestClass_AComponent_Impl implements TestClass.AComponent {",
                        "  private final TestClass.A aComponentA;",
                        "",
                        "  private TestClass_AComponent_Impl(TestClass.A aComponentA) {",
                        "    this.aComponentA = aComponentA;",
                        "  }",
                        "",
                        "  @Override",
                        "  public TestClass.A getA() {",
                        "    return aComponentA;",
                        "  }",
                        "",
                        "  static TestClass.AComponent create() {",
                        "    TestClass.B aComponentB = TestClass.AComponent.createB1();",
                        "    TestClass.B aComponentB2 = TestClass.AComponent.createB2();",
                        "    TestClass.A aComponentA = TestClass.AComponent.createA(aComponentB, aComponentB2);",
                        "    return new TestClass_AComponent_Impl(aComponentA);",
                        "  }",
                        "}");
    }
}
