package io.jbock.simple.processor.util;

import com.palantir.javapoet.JavaFile;
import io.jbock.simple.Inject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;

import static javax.tools.Diagnostic.Kind.ERROR;

@Inject
public record SourceFileGenerator(Filer filer, Messager messager) {

    public void write(Element element, JavaFile javaFile) {
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();
            messager.printMessage(ERROR, stack, element);
        }
    }
}
