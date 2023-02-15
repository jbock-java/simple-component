package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class TypeParametersTest {

    @Test
    void requestMethodWithTypeParameter() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import java.util.List;",
                "",
                "final class TestClass {",
                "",
                "  static class A {",
                "    @Inject A() {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    <E> A getA();",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Type parameters are not allowed");
    }

    @Test
    void componentWithTypeParameter() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import java.util.List;",
                "",
                "final class TestClass {",
                "",
                "  static class A {",
                "    @Inject A() {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent<E> {",
                "    A getA();",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Type parameters are not allowed");
    }

    @Test
    void injectedTypeWithTypeParameter() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import java.util.List;",
                "",
                "final class TestClass {",
                "",
                "  static class A {",
                "    @Inject A(B<String> b) {}",
                "  }",
                "",
                "  static class B<E> {",
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
        assertThat(compilation).hadErrorContaining("Type parameters are not allowed");
    }

    @Test
    void factoryWithTypeParameter() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "",
                "final class TestClass {",
                "  static class A {",
                "    @Inject A(@Named(\"b\") int i) {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    String getS();",
                "",
                "    @Component.Factory",
                "    interface Factory<E> {",
                "      AComponent create(String s);",
                "    }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Type parameters are not allowed");
    }

    @Test
    void factoryMethodWithTypeParameter() {
        JavaFileObject component = forSourceLines("test.TestClass",
                "package test;",
                "",
                "import io.jbock.simple.Component;",
                "import io.jbock.simple.Inject;",
                "import java.util.List;",
                "",
                "final class TestClass {",
                "",
                "  static class A {",
                "    @Inject A() {}",
                "  }",
                "",
                "  @Component",
                "  interface AComponent {",
                "    A getA();",
                "",
                "    @Component.Factory",
                "    interface Factory {",
                "      <E> AComponent create(List<E> s);",
                "    }",
                "  }",
                "}");
        Compilation compilation = simpleCompiler().compile(component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Type parameters are not allowed");
    }
}
