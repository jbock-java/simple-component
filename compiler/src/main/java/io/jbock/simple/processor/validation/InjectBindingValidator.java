package io.jbock.simple.processor.validation;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.InjectBindingScanner;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.util.Visitors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;

import static io.jbock.simple.processor.util.TypeNames.JAKARTA_INJECT;
import static io.jbock.simple.processor.util.TypeNames.JAVAX_INJECT;
import static io.jbock.simple.processor.util.TypeNames.SIMPLE_INJECT;

public final class InjectBindingValidator {

    private final TypeTool tool;
    private final InjectBindingScanner injectBindingScanner;

    @Inject
    public InjectBindingValidator(
            TypeTool tool,
            InjectBindingScanner injectBindingScanner) {
        this.tool = tool;
        this.injectBindingScanner = injectBindingScanner;
    }

    public void validateConstructor(ExecutableElement element) {
        validate(element);
    }

    private void validate(ExecutableElement element) {
        TypeElement typeElement = Visitors.TYPE_ELEMENT_VISITOR.visit(element.getEnclosingElement());
        if (!typeElement.getTypeParameters().isEmpty()) {
            throw new ValidationFailure("Type parameters are not allowed on element", typeElement);
        }
        List<ExecutableElement> methods = injectBindingScanner.scan(typeElement);
        for (ExecutableElement m : methods) {
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
}
