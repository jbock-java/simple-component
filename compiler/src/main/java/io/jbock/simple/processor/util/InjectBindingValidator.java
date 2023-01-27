package io.jbock.simple.processor.util;

import io.jbock.simple.Inject;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import java.util.List;

public final class InjectBindingValidator {

    private final TypeTool tool;

    public InjectBindingValidator(TypeTool tool) {
        this.tool = tool;
    }

    public void validateConstructor(ExecutableElement element) {
        validate(element);
    }

    public void validateStaticMethod(ExecutableElement element) {
        validate(element);
        if (!tool.types().isSameType(element.getReturnType(), element.getEnclosingElement().asType())) {
            throw new ValidationFailure("Static method binding must return the enclosing type",
                    element);
        }
    }

    private void validate(ExecutableElement element) {
        TypeElement typeElement = Visitors.TYPE_ELEMENT_VISITOR.visit(element.getEnclosingElement());
        if (!typeElement.getTypeParameters().isEmpty()) {
            throw new ValidationFailure("Type parameters are not allowed on element", typeElement);
        }
        List<? extends Element> allMembers = tool.elements().getAllMembers(typeElement);
        List<ExecutableElement> constructors = ElementFilter.constructorsIn(allMembers).stream()
                .filter(c -> c.getAnnotationMirrors().stream().anyMatch(m -> tool.isSameType(m.getAnnotationType(), Inject.class)))
                .toList();
        List<ExecutableElement> methods = ElementFilter.methodsIn(allMembers).stream()
                .filter(c -> c.getAnnotationMirrors().stream().anyMatch(m -> tool.isSameType(m.getAnnotationType(), Inject.class)))
                .toList();
        if (constructors.size() >= 2) {
            throw new ValidationFailure("Only one constructor binding per class allowed",
                    element);
        }
        if (methods.size() >= 2) {
            throw new ValidationFailure("Only one static method binding per class allowed",
                    element);
        }
        if (!constructors.isEmpty() && !methods.isEmpty()) {
            throw new ValidationFailure(
                    "Static method bindings are not allowed in a class with a constructor binding",
                    element);
        }
        if (!methods.isEmpty()) {
            ExecutableElement m = methods.get(0);
            if (!m.getModifiers().contains(Modifier.STATIC)) {
                throw new ValidationFailure("The factory method must be static", m);
            }
            if (m.getReturnType().getKind() == TypeKind.VOID) {
                throw new ValidationFailure("The factory method may not return void", m);
            }
            if (!tool.types().isSameType(m.getReturnType(), typeElement.asType())) {
                throw new ValidationFailure("The factory method must return the type of its enclosing class", m);
            }
        }
    }
}
