package io.jbock.simple.processor.util;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static javax.tools.Diagnostic.Kind.ERROR;

public final class ValidationFailure extends RuntimeException {

    private final Element about;
    private final String message;

    public ValidationFailure(String message, Element about) {
        this.message = message;
        this.about = about;
    }

    public void writeTo(Messager messager) {
        messager.printMessage(ERROR, message, about);
    }

    public void writeTo(Messager messager, TypeElement component) {
        messager.printMessage(ERROR, "While validating " + component.getSimpleName() + ": " + message, component);
    }
}
