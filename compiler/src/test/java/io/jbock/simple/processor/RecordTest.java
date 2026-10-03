package io.jbock.simple.processor;

import io.jbock.testing.compile.Compilation;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static io.jbock.simple.processor.Compilers.simpleCompiler;
import static io.jbock.testing.compile.CompilationSubject.assertThat;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class RecordTest {

    @Test
    void annotatedClass() {
        JavaFileObject baka = forSourceLines("test.Baka",
                "package test;",
                "import io.jbock.simple.Inject;",
                "",
                "class Baka {",
                "  @Inject Baka() {}",
                "}");
        Compilation compilation = simpleCompiler().compile(baka, FailTest.bakaTestComponent());
        assertThat(compilation).succeeded();
    }

    @Test
    void annotatedRecord() {
        JavaFileObject baka = forSourceLines("test.Baka",
                "package test;",
                "import io.jbock.simple.Inject;",
                "",
                "@Inject",
                "record Baka() {",
                "}");
        Compilation compilation = simpleCompiler().compile(baka, FailTest.bakaTestComponent());
        assertThat(compilation).succeeded();
    }
}
