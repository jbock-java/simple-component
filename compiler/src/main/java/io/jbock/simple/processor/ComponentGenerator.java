package io.jbock.simple.processor;

import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.Edge;
import io.jbock.simple.processor.util.InjectBindingRegistry;
import io.jbock.simple.processor.util.ValidationFailure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ComponentGenerator {

    private final InjectBindingRegistry injectBindingRegistry;

    public ComponentGenerator(
            InjectBindingRegistry injectBindingRegistry) {
        this.injectBindingRegistry = injectBindingRegistry;
    }

    class Generator {
        private final ComponentElement component;

        Generator(ComponentElement component) {
            this.component = component;
        }

        public TypeSpec generate() {
            Set<Edge> edges = new LinkedHashSet<>();
            for (DependencyRequest request : component.getRequests()) {
                edges.addAll(injectBindingRegistry.getDependencies(request));
            }
            List<InjectBinding> sorted = sortEdges(edges);
            return TypeSpec.classBuilder(component.generatedClass())
                    .addOriginatingElement(component.element())
                    .build();
        }

        // https://en.wikipedia.org/wiki/Topological_sorting
        private List<InjectBinding> sortEdges(Set<Edge> edges) {
            List<InjectBinding> result = new ArrayList<>();
            Deque<InjectBinding> s = new ArrayDeque<>(startNodes(edges));
            Graph graph = createGraph(edges);
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
        return new Generator(component).generate();
    }

    private List<InjectBinding> startNodes(Set<Edge> edges) {
        List<InjectBinding> requests = edges.stream().map(Edge::dependency).toList();
        return requests.stream()
                .filter(r -> edges.stream().noneMatch(edge -> edge.destination().key().equals(r.key())))
                .toList();
    }

    private static class Graph {

        final Set<Edge> edges;
        final Set<InjectBinding> nodes;

        Graph(Set<Edge> edges, Set<InjectBinding> nodes) {
            this.edges = edges;
            this.nodes = nodes;
        }

        List<Edge> edgesFrom(InjectBinding n) {
            return edges.stream().filter(edge -> edge.source().equals(n)).toList();
        }

        List<Edge> edgesTo(InjectBinding m) {
            return edges.stream().filter(edge -> edge.destination().equals(m)).toList();
        }

        void removeEdge(Edge edge) {
            edges.remove(edge);
        }
    }

    private Graph createGraph(Set<Edge> edges) {
        Set<InjectBinding> nodes = new LinkedHashSet<>();
        for (Edge edge : edges) {
            nodes.add(edge.source());
            nodes.add(edge.destination());
        }
        return new Graph(new LinkedHashSet<>(edges), nodes);
    }
}
