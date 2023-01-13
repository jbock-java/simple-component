package io.jbock.simple.processor.util;

import java.util.LinkedHashSet;
import java.util.Set;

public class ComponentRegistry {

    private final Set<ComponentElement> components = new LinkedHashSet<>();

    public void registerComponent(ComponentElement typeElement) {
        components.add(typeElement);
    }

    public Set<ComponentElement> components() {
        return components;
    }
}
