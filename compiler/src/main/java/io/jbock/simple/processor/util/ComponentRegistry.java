package io.jbock.simple.processor.util;

import javax.lang.model.element.TypeElement;
import java.util.LinkedHashSet;
import java.util.Set;

public class ComponentRegistry {

    private final Set<TypeElement> components = new LinkedHashSet<>();

    public void registerComponent(TypeElement typeElement) {
        components.add(typeElement);
    }

    public Set<TypeElement> components() {
        return components;
    }
}
