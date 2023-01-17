package io.jbock.simple.processor.util;

import javax.lang.model.element.TypeElement;

public final class ComponentElementValidator {

    public void validate(TypeElement element) {
        if (!element.getTypeParameters().isEmpty()) {
            throw new ValidationFailure("Type parameters are not allowed on component", element);
        }
    }
}
