package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import io.jbock.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class MissingBindingTest {

    @Test
    void missingBinding() {
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
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "  }",
                "}");

        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContainingMatch("java.lang.String cannot be provided.")
                .inFile(component)
                .onLineContaining("interface AComponent");
    }

    @Test
    void bindsMethodAppearsInTrace() {
        JavaFileObject component =
                JavaFileObjects.forSourceLines("p.TestComponent",
                        "package p;",
                        "",
                        "import io.jbock.simple.Component;",
                        "import io.jbock.simple.Provides;",
                        "",
                        "@Component",
                        "interface TestComponent {",
                        "  TestInterface testInterface();",
                        "",
                        "  @Provides static TestInterface bindTestInterface(TestImplementation implementation) {",
                        "    return implementation;",
                        "  }",
                        "}");
        JavaFileObject interfaceFile =
                JavaFileObjects.forSourceLines("p.TestInterface",
                        "package p;",
                        "",
                        "interface TestInterface {}");
        JavaFileObject implementationFile =
                JavaFileObjects.forSourceLines("p.TestImplementation",
                        "package p;",
                        "",
                        "import io.jbock.simple.Inject;",
                        "",
                        "final class TestImplementation implements TestInterface {",
                        "  @Inject TestImplementation(String missingBinding) {}",
                        "}");

        Compilation compilation =
                simpleCompiler().compile(component, interfaceFile, implementationFile);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorCount(1);
        assertThat(compilation)
                .hadErrorContaining(
                        TestUtils.message(
                                "java.lang.String cannot be provided.",
                                "    java.lang.String is injected at",
                                "        TestImplementation(java.lang.String)",
                                "    p.TestImplementation is injected at",
                                "        TestComponent.bindTestInterface(p.TestImplementation)",
                                "    p.TestInterface is requested at",
                                "        TestComponent.testInterface()"))
                .inFile(component)
                .onLineContaining("interface TestComponent");
    }
}