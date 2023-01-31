package io.jbock.simple.processor.graph;

import io.jbock.simple.processor.binding.Binding;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class Graph {

    private final Set<Edge> edges;
    private final Set<Binding> nodes;

    Graph(Set<Edge> edges, Set<Binding> nodes) {
        this.edges = edges;
        this.nodes = nodes;
    }

    void addAll(Graph other) {
        edges.addAll(other.edges);
        nodes.addAll(other.nodes);
    }

    static Graph newGraph() {
        return new Graph(new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    List<Edge> edgesFrom(Binding n) {
        return edges.stream().filter(edge -> edge.source().equals(n)).collect(Collectors.toList());
    }

    List<Edge> edgesTo(Binding m) {
        return edges.stream().filter(edge -> edge.destination().equals(m)).collect(Collectors.toList());
    }

    List<Binding> startNodes() {
        return nodes.stream()
                .filter(r -> edgesTo(r).isEmpty())
                .collect(Collectors.toList());
    }

    void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    Set<Binding> nodes() {
        return nodes;
    }

    Set<Edge> edges() {
        return edges;
    }
}
