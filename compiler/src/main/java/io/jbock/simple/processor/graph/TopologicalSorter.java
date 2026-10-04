package io.jbock.simple.processor.graph;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.Node;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.KeyFactory;
import io.jbock.simple.processor.binding.ParameterBinding;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class TopologicalSorter {

    private final GraphFactory graphFactory;
    private final ComponentElement component;
    private final KeyFactory keyFactory;

    @Inject
    public TopologicalSorter(
            GraphFactory graphFactory,
            ComponentElement component,
            KeyFactory keyFactory) {
        this.graphFactory = graphFactory;
        this.component = component;
        this.keyFactory = keyFactory;
    }

    public List<Node> sortedBindings() {
        AccessibilityValidator validator = AccessibilityValidator.create(component);
        Graph graph = Graph.newGraph();
        for (ParameterBinding request : keyFactory.parameterBindings().values()) {
            // preserve parameter order
            graph.nodes().add(request);
        }
        for (DependencyRequest request : keyFactory.requests()) {
            graph.addAll(graphFactory.getGraph(request));
        }
        for (Node binding : graph.nodes()) {
            if (!(binding instanceof ParameterBinding)) {
                validator.checkAccessible(binding.element());
            }
        }
        return sort(graph);
    }

    // https://en.wikipedia.org/wiki/Topological_sorting
    List<Node> sort(Graph graph) {
        List<Node> result = new ArrayList<>(graph.nodes().size());
        Deque<Node> s = new ArrayDeque<>(graph.startNodes());
        while (!s.isEmpty()) {
            Node n = s.pop();
            result.add(n);
            for (Edge e : graph.edgesFrom(n)) {
                graph.removeEdge(e);
                Node m = e.destination();
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
