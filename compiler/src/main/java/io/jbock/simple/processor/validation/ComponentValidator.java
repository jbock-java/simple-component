package io.jbock.simple.processor.validation;

import io.jbock.simple.Provides;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

public final class ComponentValidator {

    private final Messager messager;

    public ComponentValidator(Messager messager) {
        this.messager = messager;
    }

    public void validate(TypeElement element) {
        for (ExecutableElement m : ElementFilter.methodsIn(element.getEnclosedElements())) {
            if (m.getModifiers().contains(Modifier.STATIC) && m.getAnnotation(Provides.class) == null) {
                messager.printMessage(Diagnostic.Kind.NOTE, "Maybe this static method should have a @Provides annotation?", m);
            }
        }
    }
}
