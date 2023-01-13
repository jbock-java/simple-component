package io.jbock.simple.processor.writing;

import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.Edge;
import io.jbock.simple.processor.util.InjectBindingRegistry;
import io.jbock.simple.processor.util.UniqueNameSet;
import io.jbock.simple.processor.util.ValidationFailure;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComponentGenerator {

    private final InjectBindingRegistry injectBindingRegistry;
    private final ComponentImpl componentImpl;

    public ComponentGenerator(
            InjectBindingRegistry injectBindingRegistry,
            ComponentImpl componentImpl) {
        this.injectBindingRegistry = injectBindingRegistry;
        this.componentImpl = componentImpl;
    }

    private class Generator {
        final ComponentElement component;

        Generator(ComponentElement component) {
            this.component = component;
        }

        Set<InjectBinding> generate() {
            Set<Edge> edges = new LinkedHashSet<>();
            Set<InjectBinding> nodes = new LinkedHashSet<>();
            for (DependencyRequest request : component.getRequests()) {
                InjectBinding binding = injectBindingRegistry.getBinding(request);
                nodes.add(binding);
                edges.addAll(injectBindingRegistry.getDependencies(binding));
            }
            return sortEdges(new Graph(edges, nodes));
        }

        // https://en.wikipedia.org/wiki/Topological_sorting
        Set<InjectBinding> sortEdges(Graph graph) {
            Set<InjectBinding> result = new LinkedHashSet<>();
            Deque<InjectBinding> s = new ArrayDeque<>(graph.startNodes());
            while (!s.isEmpty()) {
                InjectBinding n = s.pop();
                result.add(n);
                for (Edge e : graph.edgesFrom(n)) {
                    graph.removeEdge(e);
                    InjectBinding m = e.destination();
                    if (graph.edgesTo(m).isEmpty()) {
                        s.push(m);
                    }
                }
            }
            if (!graph.edges.isEmpty()) {
                throw new ValidationFailure("cycle detected", component.element());
            }
            return result;
        }
    }

    public TypeSpec generate(ComponentElement component) {
        Set<InjectBinding> sorted = new Generator(component).generate();
        return componentImpl.generate(component, addNames(sorted));
    }

    Map<Key, NamedBinding> addNames(Set<InjectBinding> sorted) {
        UniqueNameSet uniqueNameSet = new UniqueNameSet();
        Map<Key, NamedBinding> result = new LinkedHashMap<>();
        for (InjectBinding b : sorted) {
            String name = uniqueNameSet.getUniqueName(b.key().suggestedVariableName());
            result.put(b.key(), new NamedBinding(b, name));
        }
        return result;
    }

    private record Graph(Set<Edge> edges, Set<InjectBinding> nodes) {

        List<Edge> edgesFrom(InjectBinding n) {
            return edges.stream().filter(edge -> edge.source().equals(n)).toList();
        }

        List<Edge> edgesTo(InjectBinding m) {
            return edges.stream().filter(edge -> edge.destination().equals(m)).toList();
        }

        List<InjectBinding> startNodes() {
            return nodes.stream()
                    .filter(r -> edgesTo(r).isEmpty())
                    .toList();
        }

        void removeEdge(Edge edge) {
            edges.remove(edge);
        }
    }
}
