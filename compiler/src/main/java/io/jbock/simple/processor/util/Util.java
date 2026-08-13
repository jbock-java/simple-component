package io.jbock.simple.processor.util;

import java.util.List;
import javax.lang.model.element.TypeElement;

public final class Util {

    public static List<TypeElement> getWithEnclosing(TypeElement typeElement) {
        if (typeElement == null) {
            return List.of();
        }
        TypeElement el = Visitors.TYPE_ELEMENT_VISITOR.visit(typeElement.getEnclosingElement());
        return el == null ? List.of(typeElement) : List.of(typeElement, el);
    }

    private Util() {
    }
}
