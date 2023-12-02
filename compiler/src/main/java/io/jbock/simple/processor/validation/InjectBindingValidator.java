package io.jbock.simple.processor.validation;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.InjectBindingFactory;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.util.Visitors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import java.util.Map;

import static io.jbock.simple.processor.util.TypeNames.JAKARTA_INJECT;
import static io.jbock.simple.processor.util.TypeNames.JAVAX_INJECT;
import static io.jbock.simple.processor.util.TypeNames.SIMPLE_INJECT;

public final class InjectBindingValidator {

    private final TypeTool tool;
    private final InjectBindingFactory injectBindingFactory;

    @Inject
    public InjectBindingValidator(
            TypeTool tool,
            InjectBindingFactory injectBindingFactory) {
        this.tool = tool;
        this.injectBindingFactory = injectBindingFactory;
    }

    public void validateConstructor(ExecutableElement element) {
        validate(element);
    }

    public void validateStaticMethod(ExecutableElement method) {
        validate(method);
        TypeElement typeElement = Visitors.TYPE_ELEMENT_VISITOR.visit(method.getEnclosingElement());
//        List<TypeElement> hierarchyMethod = getEnclosingElements(typeElement);
//        List<TypeElement> hierarchyRt = tool.types().asElement(method.getReturnType())
//                .map(Visitors.TYPE_ELEMENT_VISITOR::visit)
//                .map(this::getEnclosingElements)
//                .orElse(List.of());
        if (!method.getModifiers().contains(Modifier.STATIC)) {
            throw new ValidationFailure("The factory method must be static", method);
        }
        if (method.getReturnType().getKind() == TypeKind.VOID) {
            throw new ValidationFailure("The factory method may not return void", method);
        }
        if (!tool.types().isSameType(method.getReturnType(), typeElement.asType())) {
            throw new ValidationFailure("The factory method must return the type of its enclosing class", method);
        }
    }

/*
    private List<TypeElement> getEnclosingElements(TypeElement typeElement) {
        if (typeElement == null) {
            return List.of();
        }
        List<TypeElement> acc = new ArrayList<>(2);
        acc.add(typeElement);
        TypeElement el = typeElement;
        if ((el = Visitors.TYPE_ELEMENT_VISITOR.visit(el.getEnclosingElement())) != null) {
            acc.add(el);
        }
        return acc;
    }
*/

    private void validate(ExecutableElement element) {
        TypeElement typeElement = Visitors.TYPE_ELEMENT_VISITOR.visit(element.getEnclosingElement());
        if (!typeElement.getTypeParameters().isEmpty()) {
            throw new ValidationFailure("Type parameters are not allowed on element", typeElement);
        }
        Map<Key, InjectBinding> m = injectBindingFactory.injectBindings(typeElement);
        for (InjectBinding b : m.values()) {
            if (b.element().getAnnotationMirrors().stream().filter(mirror -> {
                DeclaredType annotationType = mirror.getAnnotationType();
                return tool.isSameType(annotationType, JAVAX_INJECT)
                        || tool.isSameType(annotationType, JAKARTA_INJECT)
                        || tool.isSameType(annotationType, SIMPLE_INJECT);
            }).count() >= 2) {
                throw new ValidationFailure("Duplicate inject annotation", b.element());
            }
        }
    }
}
