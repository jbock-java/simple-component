package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.Component;
import io.jbock.simple.processor.util.ComponentRegistry;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComponentStep implements Step {

    private final ComponentRegistry registry;

    public ComponentStep(
            ComponentRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(Component.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        List<Element> elements = elementsByAnnotation.values().stream().flatMap(Set::stream).toList();
        List<TypeElement> typeElements = ElementFilter.typesIn(elements);
        for (TypeElement typeElement : typeElements) {
            registry.registerComponent(typeElement);
        }
        return Set.of();
    }
}
