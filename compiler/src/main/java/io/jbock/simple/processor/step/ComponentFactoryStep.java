package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.validation.ExecutableElementValidator;
import io.jbock.simple.processor.validation.TypeElementValidator;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentFactoryStep implements Step {

    private final TypeElementValidator validator;
    private final ExecutableElementValidator executableElementValidator;
    private final Messager messager;

    @Inject
    public ComponentFactoryStep(
            Messager messager,
            TypeElementValidator validator,
            ExecutableElementValidator executableElementValidator) {
        this.messager = messager;
        this.validator = validator;
        this.executableElementValidator = executableElementValidator;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(
                Component.Factory.class.getCanonicalName(),
                Component.Builder.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        try {
            List<Element> elements = elementsByAnnotation.values().stream()
                    .flatMap(Set::stream)
                    .collect(Collectors.toList());
            List<TypeElement> typeElements = ElementFilter.typesIn(elements);
            for (TypeElement typeElement : typeElements) {
                validator.validate(typeElement);
                Element enclosing = typeElement.getEnclosingElement();
                if (enclosing.getAnnotation(Component.class) == null) {
                    throw new ValidationFailure("The @Factory must be nested inside a @Component", typeElement);
                }
                List<? extends Element> siblings = enclosing.getEnclosedElements();
                if (siblings.stream()
                        .filter(sibling -> sibling.getAnnotation(Component.Factory.class) != null
                                || sibling.getAnnotation(Component.Builder.class) != null)
                        .count() >= 2) {
                    throw new ValidationFailure("Only one @Factory or @Builder allowed", enclosing);
                }
                for (ExecutableElement m : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (m.getModifiers().contains(Modifier.ABSTRACT)) {
                        executableElementValidator.validate(m);
                    }
                }
            }
        } catch (ValidationFailure f) {
            f.writeTo(messager);
        }
        return Set.of();
    }
}
