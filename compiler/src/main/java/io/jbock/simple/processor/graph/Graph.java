package io.jbock.simple.processor.graph;

import io.jbock.simple.processor.binding.Node;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

record Graph(Set<Edge> edges, Set<Node> nodes) {

    void addAll(Graph other) {
        edges.addAll(other.edges);
        nodes.addAll(other.nodes);
    }

    static Graph newGraph() {
        return new Graph(new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    List<Edge> edgesFrom(Node n) {
        return edges.stream().filter(edge -> edge.source().equals(n)).collect(Collectors.toList());
    }

    List<Edge> edgesTo(Node m) {
        return edges.stream().filter(edge -> edge.destination().equals(m)).collect(Collectors.toList());
    }

    List<Node> startNodes() {
        return nodes.stream()
                .filter(r -> edgesTo(r).isEmpty())
                .collect(Collectors.toList());
    }

    void removeEdge(Edge edge) {
        edges.remove(edge);
    }
}
