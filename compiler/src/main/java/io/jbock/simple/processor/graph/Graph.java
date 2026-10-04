package io.jbock.simple.processor.graph;

import io.jbock.simple.processor.binding.Node;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

record Graph(Set<AbstractEdge<Node>> edges, Set<Node> nodes) {

    void addAll(Graph other) {
        edges.addAll(other.edges);
        nodes.addAll(other.nodes);
    }

    static Graph newGraph() {
        return new Graph(new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    List<AbstractEdge<Node>> edgesFrom(Node n) {
        return edges.stream()
                .filter(edge -> edge.source().equals(n))
                .toList();
    }

    List<AbstractEdge<Node>> edgesTo(Node m) {
        return edges.stream()
                .filter(edge -> edge.destination().equals(m))
                .toList();
    }

    List<Node> startNodes() {
        return nodes.stream()
                .filter(r -> edgesTo(r).isEmpty())
                .toList();
    }

    void removeEdge(AbstractEdge<Node> edge) {
        edges.remove(edge);
    }
}
