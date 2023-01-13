package io.jbock.simple.processor;

import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.Edge;
import io.jbock.simple.processor.util.InjectBindingRegistry;
import io.jbock.simple.processor.util.ValidationFailure;

import java.util.ArrayDeque;
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
            Set<InjectBinding> sorted = sortEdges(edges);
            return TypeSpec.classBuilder(component.generatedClass())
                    .addOriginatingElement(component.element())
                    .build();
        }

        // https://en.wikipedia.org/wiki/Topological_sorting
        private Set<InjectBinding> sortEdges(Set<Edge> edges) {
            Set<InjectBinding> result = new LinkedHashSet<>();
            Graph graph = createGraph(edges);
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
        return new Generator(component).generate();
    }

    private record Graph(Set<Edge> edges, Set<InjectBinding> nodes) {

        List<Edge> edgesFrom(InjectBinding n) {
                return edges.stream().filter(edge -> edge.source().equals(n)).toList();
            }
    
            List<Edge> edgesTo(InjectBinding m) {
                return edges.stream().filter(edge -> edge.destination().equals(m)).toList();
            }
    
            List<InjectBinding> startNodes() {
                List<InjectBinding> sources = edges.stream().map(Edge::source).toList();
                return sources.stream()
                        .filter(r -> edgesTo(r).isEmpty())
                        .toList();
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
