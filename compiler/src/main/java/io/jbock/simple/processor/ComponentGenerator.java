package io.jbock.simple.processor;

import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.InjectBindingRegistry;

public class ComponentGenerator {

    private final InjectBindingRegistry injectBindingRegistry;

    public ComponentGenerator(
            InjectBindingRegistry injectBindingRegistry) {
        this.injectBindingRegistry = injectBindingRegistry;
    }

    public TypeSpec generate(ComponentElement component) {
        return TypeSpec.classBuilder(component.generatedClass())
                .addOriginatingElement(component.element())
                .build();
    }
}
