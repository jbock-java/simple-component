package io.jbock.simple.processor.graph;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.ParameterBinding;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

public final class TopologicalSorter {

    private final GraphFactory graphFactory;
    private final ComponentElement component;

    @Inject
    public TopologicalSorter(
            GraphFactory graphFactory,
            ComponentElement component) {
        this.graphFactory = graphFactory;
        this.component = component;
    }

    public Set<Binding> analyze() {
        AccessibilityValidator validator = AccessibilityValidator.create(component);
        Graph graph = Graph.newGraph();
        for (DependencyRequest request : component.requests()) {
            graph.addAll(graphFactory.getGraph(request));
        }
        for (Binding binding : graph.nodes()) {
            if (!(binding instanceof ParameterBinding)) {
                validator.checkAccessible(binding.element());
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
}
