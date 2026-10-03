package io.jbock.simple.processor.validation;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.util.Visitors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public final class ExecutableElementValidator {

    private final TypeTool tool;

    @Inject
    public ExecutableElementValidator(TypeTool tool) {
        this.tool = tool;
    }

    public void validate(ExecutableElement element) {
        if (!element.getTypeParameters().isEmpty()) {
            throw new ValidationFailure("Type parameters are not allowed here", element);
        }
        checkExceptionsInDeclaration(element);
    }

    public void checkExceptionsInDeclaration(ExecutableElement el) throws ValidationFailure {
        for (TypeMirror thrownType : el.getThrownTypes()) {
            if (isChecked(thrownType)) {
                throw new ValidationFailure("Checked exception is not allowed here.", el);
            }
        }
    }

    private boolean isChecked(TypeMirror typeMirror) {
        TypeMirror check = typeMirror;
        while (true) {
            if (check == null) {
                return false;
            }
            if (check.getKind() == TypeKind.NONE) {
                return false;
            }
            Optional<TypeElement> typeElement = tool.types().asElement(check).map(Visitors.TYPE_ELEMENT_VISITOR::visit);
            if (typeElement.isEmpty()) {
                return false;
            }
            TypeElement tel = typeElement.orElseThrow();
            if (tel.getQualifiedName().contentEquals("java.lang.RuntimeException")) {
                return false;
            }
            if (tel.getQualifiedName().contentEquals("java.lang.Exception")) {
                return true;
            }
            check = tel.getSuperclass();
        }
    }
}
