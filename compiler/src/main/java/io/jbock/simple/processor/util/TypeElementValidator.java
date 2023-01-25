package io.jbock.simple.processor.util;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

public final class TypeElementValidator {

    public void validate(TypeElement element) {
        if (element.getKind() != ElementKind.INTERFACE) {
            throw new ValidationFailure("The annotated class must be an interface", element);
        }
        if (!element.getTypeParameters().isEmpty()) {
            throw new ValidationFailure("Type parameters are not allowed here", element);
        }
        checkNesting(element);
    }

    public void checkNesting(TypeElement classToCheck) {
        if (classToCheck.getNestingKind().isNested() && !classToCheck.getModifiers().contains(STATIC)) {
            throw new ValidationFailure("nested class '" +
                    classToCheck.getSimpleName() +
                    "' must be static", classToCheck);
        }
        if (classToCheck.getModifiers().contains(PRIVATE)) {
            throw new ValidationFailure("class '" +
                    classToCheck.getSimpleName() +
                    " may not be private", classToCheck);
        }
        for (TypeElement element : getEnclosingElements(classToCheck)) {
            if (element.getModifiers().contains(PRIVATE)) {
                throw new ValidationFailure("enclosing class '" +
                        element.getSimpleName() +
                        "' may not be private", classToCheck);
            }
            if (element.getNestingKind().isNested() && !element.getModifiers().contains(STATIC)) {
                throw new ValidationFailure("nested class '" +
                        element.getSimpleName() +
                        "' must be static", classToCheck);
            }
        }
    }

    private List<TypeElement> getEnclosingElements(TypeElement sourceElement) {
        if (!sourceElement.getNestingKind().isNested()) {
            return List.of();
        }
        List<TypeElement> result = new ArrayList<>();
        TypeElement current = sourceElement;
        while (current.getNestingKind().isNested()) {
            TypeElement enclosing = TYPE_ELEMENT_VISITOR.visit(current.getEnclosingElement());
            if (enclosing == null) {
                break;
            }
            result.add(enclosing);
            current = enclosing;
        }
        return result;
    }
}
