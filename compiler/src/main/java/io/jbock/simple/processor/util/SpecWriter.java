package io.jbock.simple.processor.util;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import io.jbock.simple.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

@Inject
public record SpecWriter(
        SourceFileGenerator sourceFileGenerator,
        Messager messager) {

    public void write(ClassName generatedClass, TypeSpec typeSpec) {
        try {
            writeSpec(generatedClass, typeSpec);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            messager.printMessage(Diagnostic.Kind.ERROR, errors.toString(), typeSpec.originatingElements().get(0));
        }
    }

    private void writeSpec(ClassName generatedClass, TypeSpec typeSpec) {
        if (typeSpec.originatingElements().size() != 1) {
            throw new AssertionError();
        }
        String packageName = generatedClass.packageName();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .skipJavaLangImports(true)
                .build();
        sourceFileGenerator.write(typeSpec.originatingElements().get(0), javaFile);
    }
}
