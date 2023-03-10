package io.jbock.simple.processor.validation;

import io.jbock.javapoet.TypeName;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.util.Visitors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public final class ExecutableElementValidator {

    private final TypeTool tool;
    private final TypeElementValidator typeElementValidator;

    @Inject
    public ExecutableElementValidator(
            TypeTool tool,
            TypeElementValidator typeElementValidator) {
        this.tool = tool;
        this.typeElementValidator = typeElementValidator;
    }

    public void validate(ExecutableElement element) {
        if (!element.getTypeParameters().isEmpty()) {
            throw new ValidationFailure("Type parameters are not allowed here", element);
        }
        checkExceptionsInDeclaration(element);
    }

    public void checkExceptionsInDeclaration(ExecutableElement element) {
        for (TypeMirror thrown : element.getThrownTypes()) {
            checkChecked(element, thrown, thrown);
        }
    }

    private void checkChecked(
            ExecutableElement element,
            TypeMirror thrown,
            TypeMirror mirror) {
        if (tool.isSameType(mirror, RuntimeException.class) ||
                tool.isSameType(mirror, Error.class)) {
            return;
        }
        if (tool.isSameType(mirror, Throwable.class)) {
            throw new ValidationFailure("invalid throws clause:" +
                    " found checked exception " +
                    TypeName.get(thrown), element);
        }
        Optional<TypeElement> tel = tool.types().asElement(thrown)
                .flatMap(el -> Optional.ofNullable(Visitors.TYPE_ELEMENT_VISITOR.visit(el)));
        if (tel.isEmpty()) {
            return;
        }
        TypeElement typeElement = tel.orElseThrow();
        typeElementValidator.checkNesting(typeElement);
        checkChecked(element, thrown, typeElement.getSuperclass());
    }
}
