package io.jbock.simple.processor.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static io.jbock.simple.processor.util.TypeNames.JAKARTA_INJECT;
import static io.jbock.simple.processor.util.TypeNames.JAVAX_INJECT;
import static io.jbock.simple.processor.util.TypeNames.SIMPLE_INJECT;

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

    public boolean hasInjectAnnotation(Element m) {
        if (m.getKind() != ElementKind.CONSTRUCTOR && m.getKind() != ElementKind.METHOD) {
            return false;
        }
        if (m.getAnnotationMirrors().isEmpty()) {
            return false;
        }
        return m.getAnnotationMirrors().stream().anyMatch(mirror -> {
            DeclaredType annotationType = mirror.getAnnotationType();
            return isSameType(annotationType, JAVAX_INJECT)
                    || isSameType(annotationType, JAKARTA_INJECT)
                    || isSameType(annotationType, SIMPLE_INJECT);
        });
    }

    public SafeElements elements() {
        return elements;
    }

    public SafeTypes types() {
        return types;
    }
}
