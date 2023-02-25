package io.jbock.simple.processor.validation;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.InjectBindingFactory;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.util.Visitors;

import javax.lang.model.element.ElementKind;
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
        Map<Key, InjectBinding> m = injectBindingFactory.injectBindings(typeElement);
        for (InjectBinding b : m.values()) {
            if (b.element().getKind() == ElementKind.METHOD) {
                if (!b.element().getModifiers().contains(Modifier.STATIC)) {
                    throw new ValidationFailure("The factory method must be static", b.element());
                }
                if (b.element().getReturnType().getKind() == TypeKind.VOID) {
                    throw new ValidationFailure("The factory method may not return void", b.element());
                }
                if (!tool.types().isSameType(b.element().getReturnType(), typeElement.asType())) {
                    throw new ValidationFailure("The factory method must return the type of its enclosing class", b.element());
                }
            }
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
