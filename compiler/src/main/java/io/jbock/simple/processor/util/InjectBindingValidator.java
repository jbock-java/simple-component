package io.jbock.simple.processor.util;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import java.util.List;
import java.util.stream.Collectors;

import static io.jbock.simple.processor.util.TypeNames.JAKARTA_INJECT;
import static io.jbock.simple.processor.util.TypeNames.JAVAX_INJECT;
import static io.jbock.simple.processor.util.TypeNames.SIMPLE_INJECT;
import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

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
        List<ExecutableElement> constructors = constructorsIn(allMembers).stream()
                .filter(tool::hasInjectAnnotation)
                .collect(Collectors.toList());
        List<ExecutableElement> methods = methodsIn(allMembers).stream()
                .filter(tool::hasInjectAnnotation)
                .collect(Collectors.toList());
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
        ExecutableElement m;
        if (!methods.isEmpty()) {
            m = methods.get(0);
            if (!m.getModifiers().contains(Modifier.STATIC)) {
                throw new ValidationFailure("The factory method must be static", m);
            }
            if (m.getReturnType().getKind() == TypeKind.VOID) {
                throw new ValidationFailure("The factory method may not return void", m);
            }
            if (!tool.types().isSameType(m.getReturnType(), typeElement.asType())) {
                throw new ValidationFailure("The factory method must return the type of its enclosing class", m);
            }
        } else {
            m = constructors.get(0);
        }
        if (m.getAnnotationMirrors().stream().filter(mirror -> {
            DeclaredType annotationType = mirror.getAnnotationType();
            return tool.isSameType(annotationType, JAVAX_INJECT)
                    || tool.isSameType(annotationType, JAKARTA_INJECT)
                    || tool.isSameType(annotationType, SIMPLE_INJECT);
        }).count() >= 2) {
            throw new ValidationFailure("Duplicate inject annotation", m);
        }
    }
}
