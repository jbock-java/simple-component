package io.jbock.simple.processor.writing;

import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.util.Edge;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record Graph(Set<Edge> edges, Set<Binding> nodes) {

    void addAll(Graph other) {
        edges.addAll(other.edges);
        nodes.addAll(other.nodes);
    }

    static Graph newGraph() {
        return new Graph(new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    List<Edge> edgesFrom(Binding n) {
        return edges.stream().filter(edge -> edge.source().equals(n)).toList();
    }

    List<Edge> edgesTo(Binding m) {
        return edges.stream().filter(edge -> edge.destination().equals(m)).toList();
    }

    List<Binding> startNodes() {
        return nodes.stream()
                .filter(r -> edgesTo(r).isEmpty())
                .toList();
    }

    void removeEdge(Edge edge) {
        edges.remove(edge);
    }
}
