package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.processor.util.InjectBindingRegistry;
import jakarta.inject.Inject;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InjectStep implements Step {

    private final InjectBindingRegistry registry;

    public InjectStep(InjectBindingRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(Inject.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        List<Element> elements = elementsByAnnotation.values().stream().flatMap(Set::stream).toList();
        List<ExecutableElement> constructors = ElementFilter.constructorsIn(elements);
        List<ExecutableElement> methods = ElementFilter.methodsIn(elements);
        for (ExecutableElement constructor : constructors) {
            registry.registerConstructor(constructor);
        }
        for (ExecutableElement method : methods) {
            registry.registerFactoryMethod(method);
        }
        return Set.of();
    }
}
