package io.jbock.simple.processor.writing;

import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;
import io.jbock.simple.processor.util.BindingRegistry;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.Edge;
import io.jbock.simple.processor.util.FactoryElement;
import io.jbock.simple.processor.util.UniqueNameSet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Generator {

    private final BindingRegistry bindingRegistry;
    private final ComponentImpl componentImpl;
    private final ComponentElement component;
    private final AccessibilityValidator validator;

    private Generator(
            BindingRegistry bindingRegistry,
            ComponentImpl componentImpl,
            ComponentElement component,
            AccessibilityValidator validator) {
        this.bindingRegistry = bindingRegistry;
        this.componentImpl = componentImpl;
        this.component = component;
        this.validator = validator;
    }

    public static Generator create(
            BindingRegistry bindingRegistry,
            ComponentImpl componentImpl,
            ComponentElement component) {
        return new Generator(bindingRegistry, componentImpl, component, AccessibilityValidator.create(component));
    }

    Set<Binding> analyze() {
        Graph graph = Graph.newGraph();
        for (DependencyRequest request : component.getRequests()) {
            Binding binding = bindingRegistry.getBinding(request);
            graph.addAll(bindingRegistry.getDependencies(binding));
        }
        for (Binding binding : graph.nodes()) {
            if (binding instanceof InjectBinding) {
                InjectBinding b = (InjectBinding) binding;
                validator.checkBinding(b.element());
            }
        }
        return sortNodes(graph);
    }

    // https://en.wikipedia.org/wiki/Topological_sorting
    Set<Binding> sortNodes(Graph graph) {
        Set<Binding> result = new LinkedHashSet<>();
        Deque<Binding> s = new ArrayDeque<>(graph.startNodes());
        while (!s.isEmpty()) {
            Binding n = s.pop();
            result.add(n);
            for (Edge e : graph.edgesFrom(n)) {
                graph.removeEdge(e);
                Binding m = e.destination();
                if (graph.edgesTo(m).isEmpty()) {
                    s.push(m);
                }
            }
        }
        if (!graph.edges().isEmpty()) {
            throw new CyclePrinter(graph).fail();
        }
        return result;
    }

    public TypeSpec generate() {
        Set<Binding> sorted = analyze();
        return componentImpl.generate(component, addNames(sorted));
    }

    Map<Key, NamedBinding> addNames(Set<Binding> sorted) {
        UniqueNameSet uniqueNameSet = new UniqueNameSet();
        List<ParameterBinding> parameterBindings = component.factoryElement()
                .map(FactoryElement::parameterBindings)
                .orElse(List.of());
        Map<Key, NamedBinding> result = new LinkedHashMap<>();
        for (ParameterBinding b : parameterBindings) {
            String name = b.parameter().getSimpleName().toString();
            result.put(b.key(), new NamedBinding(b, name, component.isComponentRequest(b)));
            uniqueNameSet.claim(name);
        }
        for (Binding binding : sorted) {
            if (binding instanceof InjectBinding) {
                InjectBinding b = (InjectBinding) binding;
                String name = uniqueNameSet.getUniqueName(b.suggestedVariableName());
                result.put(b.key(), new NamedBinding(b, name, component.isComponentRequest(b)));
            }
        }
        return result;
    }
}
