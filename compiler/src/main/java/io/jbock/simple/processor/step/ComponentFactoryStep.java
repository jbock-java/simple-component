package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.Component;
import io.jbock.simple.processor.util.TypeElementValidator;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentFactoryStep implements Step {

    private final TypeElementValidator validator;
    private final Messager messager;

    public ComponentFactoryStep(
            Messager messager,
            TypeElementValidator validator) {
        this.messager = messager;
        this.validator = validator;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(Component.Factory.class.getCanonicalName());
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
                    throw new ValidationFailure("The @Factory class must be nested inside its @Component", typeElement);
                }
                if (enclosing.getEnclosedElements().stream()
                        .filter(enclosed -> enclosed.getKind().isInterface())
                        .filter(enclosed -> enclosed.getAnnotation(Component.Factory.class) != null)
                        .count() >= 2) {
                    throw new ValidationFailure("Found more than one @Factory", enclosing);
                }
            }
        } catch (ValidationFailure f) {
            f.writeTo(messager);
        }
        return Set.of();
    }
}
