package io.jbock.simple.processor.graph;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

record Graph<N>(Set<AbstractEdge<N>> edges, Set<N> nodes) {

    void addAll(Graph<N> other) {
        edges.addAll(other.edges);
        nodes.addAll(other.nodes);
    }

    static <N> Graph<N> newGraph() {
        return new Graph<>(new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    List<AbstractEdge<N>> edgesFrom(N n) {
        return edges.stream()
                .filter(edge -> edge.source().equals(n))
                .toList();
    }

    List<AbstractEdge<N>> edgesTo(N m) {
        return edges.stream()
                .filter(edge -> edge.destination().equals(m))
                .toList();
    }

    List<N> startNodes() {
        return nodes.stream()
                .filter(r -> edgesTo(r).isEmpty())
                .toList();
    }

    void removeEdge(AbstractEdge<N> edge) {
        edges.remove(edge);
    }
}
