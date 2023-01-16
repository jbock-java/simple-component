package io.jbock.simple.processor.util;

import io.jbock.javapoet.JavaFile;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static javax.tools.Diagnostic.Kind.ERROR;

public final class SourceFileGenerator {

    private final Filer filer;
    private final Messager messager;

    public SourceFileGenerator(Filer filer, Messager messager) {
        this.filer = filer;
        this.messager = messager;
    }

    public void write(Element element, JavaFile javaFile) {
        try {
            // TODO filer.createResource(...); to avoid warnings?
            // https://stackoverflow.com/questions/10094346/warning-file-for-type-insert-class-here-created-in-the-last-round-will-not
            javaFile.writeTo(filer);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();
            messager.printMessage(ERROR, stack, element);
        }
    }
}
