package io.jbock.simple.processor.util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public final class TypeTool {

    private final SafeElements elements;
    private final SafeTypes types;

    public TypeTool(SafeElements elements, SafeTypes types) {
        this.elements = elements;
        this.types = types;
    }

    /**
     * Works for classes with no type parameters.
     */
    public boolean isSameType(TypeMirror mirror, Class<?> cl) {
        return isSameType(mirror, cl.getCanonicalName());
    }

    /**
     * Works for classes with no type parameters.
     */
    public boolean isSameType(TypeMirror mirror, String canonicalName) {
        return elements.getTypeElement(canonicalName)
                .map(TypeElement::asType)
                .map(type -> types.isSameType(mirror, type))
                .orElse(false);
    }

    public SafeElements elements() {
        return elements;
    }

    public SafeTypes types() {
        return types;
    }
}
