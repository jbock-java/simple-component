package io.jbock.simple.processor.writing;

import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.util.Edge;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record Graph(Set<Edge> edges, Set<InjectBinding> nodes) {

    void addAll(Graph other) {
        edges.addAll(other.edges);
        nodes.addAll(other.nodes);
    }

    static Graph newGraph() {
        return new Graph(new LinkedHashSet<>(), new LinkedHashSet<>());
    }

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

    boolean isEdgeless() {
        return edges.isEmpty();
    }
}
