package io.jbock.simple.processor.util;

import io.jbock.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

public class FactoryElement {

    private final TypeElement element;
    private final ClassName parentClass;

    FactoryElement(TypeElement element, ClassName parentClass) {
        this.element = element;
        this.parentClass = parentClass;
    }

    public TypeElement element() {
        return element;
    }

    public ClassName generatedClass() {
        return parentClass.nestedClass(element().getSimpleName() + "_Impl");
    }
}
