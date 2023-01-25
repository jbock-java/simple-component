package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class PackageTest {

    @Test
    void inaccessibleStaticMethod() {
        JavaFileObject dd = forSourceLines("test.b.DD",
                "package test.b;",
                "",
                "import jakarta.inject.Inject;",
                "",
                "public class DD {",
                "  @Inject static DD createDD() { return null; }",
                "}");
        JavaFileObject dep = forSourceLines("test.b.Dep",
                "package test.b;",
                "",
                "import jakarta.inject.Inject;",
                "",
                "public class Dep {",
                "  @Inject public Dep(DD dd) {}",
                "}");
        JavaFileObject component = forSourceLines("test.a.AComponent",
                "package test.a;",
                "",
                "import io.jbock.simple.Component;",
                "import test.b.Dep;",
                "",
                "@Component",
                "interface AComponent {",
                "  Dep getDep();",
                "}");

        Compilation compilation = simpleCompiler().compile(dd, dep, component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("method is not accessible from test.a.AComponent");
    }

    @Test
    void inaccessibleConstructor() {
        JavaFileObject dd = forSourceLines("test.b.DD",
                "package test.b;",
                "",
                "import jakarta.inject.Inject;",
                "",
                "public class DD {",
                "  @Inject DD() {}",
                "}");
        JavaFileObject dep = forSourceLines("test.b.Dep",
                "package test.b;",
                "",
                "import jakarta.inject.Inject;",
                "",
                "public class Dep {",
                "  @Inject public Dep(DD dd) {}",
                "}");
        JavaFileObject component = forSourceLines("test.a.AComponent",
                "package test.a;",
                "",
                "import io.jbock.simple.Component;",
                "import test.b.Dep;",
                "",
                "@Component",
                "interface AComponent {",
                "  Dep getDep();",
                "}");

        Compilation compilation = simpleCompiler().compile(dd, dep, component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("constructor is not accessible from test.a.AComponent");
    }


    @Test
    void inaccessibleType() {
        JavaFileObject dd = forSourceLines("test.b.DD",
                "package test.b;",
                "",
                "import jakarta.inject.Inject;",
                "",
                "class DD {",
                "  @Inject public static DD createDD() {}",
                "}");
        JavaFileObject dep = forSourceLines("test.b.Dep",
                "package test.b;",
                "",
                "import jakarta.inject.Inject;",
                "",
                "public class Dep {",
                "  @Inject public Dep(DD dd) {}",
                "}");
        JavaFileObject component = forSourceLines("test.a.AComponent",
                "package test.a;",
                "",
                "import io.jbock.simple.Component;",
                "import test.b.Dep;",
                "",
                "@Component",
                "interface AComponent {",
                "  Dep getDep();",
                "}");

        Compilation compilation = simpleCompiler().compile(dd, dep, component);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("test.b.DD is not accessible from test.a.AComponent");
    }
}