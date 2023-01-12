package io.jbock.simple.processor;

import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.Edge;
import io.jbock.simple.processor.util.InjectBindingRegistry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ComponentGenerator {

    private final InjectBindingRegistry injectBindingRegistry;

    public ComponentGenerator(
            InjectBindingRegistry injectBindingRegistry) {
        this.injectBindingRegistry = injectBindingRegistry;
    }

    public TypeSpec generate(ComponentElement component) {
        Set<Edge> dependencies = new LinkedHashSet<>();
        for (DependencyRequest request : component.getRequests()) {
            dependencies.addAll(injectBindingRegistry.getDependencies(request));
        }
        return TypeSpec.classBuilder(component.generatedClass())
                .addOriginatingElement(component.element())
                .build();
    }

    private List<InjectBinding> startNodes(Set<Edge> edges) {
        List<InjectBinding> requests = edges.stream().map(Edge::dependency).toList();
        return requests.stream()
                .filter(r -> edges.stream().noneMatch(edge -> edge.request().key().equals(r.key())))
                .toList();
    }

    private List<InjectBinding> toposort(Set<Edge> edges) {
        List<InjectBinding> result = new ArrayList<>();
        List<InjectBinding> startNodes = startNodes(edges);
        return result;
    }
}
