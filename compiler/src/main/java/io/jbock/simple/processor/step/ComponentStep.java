package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.Component;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.ComponentElementValidator;
import io.jbock.simple.processor.util.ComponentRegistry;
import io.jbock.simple.processor.util.Qualifiers;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComponentStep implements Step {

    private final ComponentRegistry registry;
    private final Messager messager;
    private final TypeTool tool;
    private final Qualifiers qualifiers;
    private final ComponentElementValidator validator;

    public ComponentStep(
            ComponentRegistry registry,
            Messager messager,
            TypeTool tool,
            Qualifiers qualifiers,
            ComponentElementValidator validator) {
        this.registry = registry;
        this.messager = messager;
        this.tool = tool;
        this.qualifiers = qualifiers;
        this.validator = validator;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(Component.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        try {
            List<Element> elements = elementsByAnnotation.values().stream().flatMap(Set::stream).toList();
            List<TypeElement> typeElements = ElementFilter.typesIn(elements);
            for (TypeElement typeElement : typeElements) {
                validator.validate(typeElement);
                ComponentElement component = ComponentElement.create(typeElement, tool, qualifiers);
                component.factoryElement().ifPresent(factory -> {
                    ExecutableElement method = factory.singleAbstractMethod();
                    if (!tool.types().isSameType(method.getReturnType(), typeElement.asType())) {
                        throw new ValidationFailure("Factory method must return the component type", method);
                    }
                });
                registry.registerComponent(component);
            }
        } catch (ValidationFailure f) {
            f.writeTo(messager);
        }
        return Set.of();
    }
}
