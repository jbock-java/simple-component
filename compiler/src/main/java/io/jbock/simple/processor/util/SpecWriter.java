package io.jbock.simple.processor.util;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.JavaFile;
import io.jbock.javapoet.TypeSpec;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class SpecWriter {

    private final SourceFileGenerator sourceFileGenerator;
    private final Messager messager;

    public SpecWriter(SourceFileGenerator sourceFileGenerator, Messager messager) {
        this.sourceFileGenerator = sourceFileGenerator;
        this.messager = messager;
    }

    public void write(ClassName generatedClass, TypeSpec typeSpec) {
        try {
            writeSpec(generatedClass, typeSpec);
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            messager.printMessage(Diagnostic.Kind.ERROR, errors.toString(), typeSpec.originatingElements.get(0));
        }
    }

    private void writeSpec(ClassName generatedClass, TypeSpec typeSpec) {
        if (typeSpec.originatingElements.size() != 1) {
            throw new AssertionError();
        }
        String packageName = generatedClass.packageName();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .skipJavaLangImports(true)
                .build();
        sourceFileGenerator.write(typeSpec.originatingElements.get(0), javaFile);
    }
}
