package io.jbock.simple.processor.graph;

import io.jbock.simple.processor.binding.Node;
import io.jbock.simple.processor.util.ValidationFailure;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.jbock.simple.processor.util.Printing.DOUBLE_INDENT;
import static io.jbock.simple.processor.util.Printing.INDENT;
import static io.jbock.simple.processor.util.Printing.bindingElementToString;

record CyclePrinter(Graph<Node> graph) {

    ValidationFailure createValidationFailure() {
        for (Node binding : graph.nodes()) {
            Optional<List<AbstractEdge<Node>>> cycle = findProperCycle(binding);
            if (cycle.isPresent()) {
                return new ValidationFailure(createReport(cycle.orElseThrow()), binding.element());
            }
        }
        throw new AssertionError("input did not contain a cycle");
    }

    private String createReport(List<AbstractEdge<Node>> cycle) {
        List<String> message = new ArrayList<>();
        message.add("Found a dependency cycle:");
        for (AbstractEdge<Node> edge : cycle) {
            Node destination = edge.destination();
            message.add(INDENT + edge.source().key().typeName() + " is injected at");
            message.add(DOUBLE_INDENT + bindingElementToString(destination.element()));
        }
        return String.join("\n", message);
    }

    private Optional<List<AbstractEdge<Node>>> findProperCycle(Node node) {
        Set<Node> seen = new LinkedHashSet<>();
        seen.add(node);
        List<AbstractEdge<Node>> cycle = findCycle(node, List.of(), seen);
        if (cycle.isEmpty()) {
            return Optional.empty();
        }
        if (!cycle.getLast().destination().equals(node)) {
            return Optional.empty();
        }
        return Optional.of(cycle);
    }

    private List<AbstractEdge<Node>> findCycle(
            Node node,
            List<AbstractEdge<Node>> current,
            Set<Node> seen) {
        List<AbstractEdge<Node>> edgesFrom = graph.edgesFrom(node);
        for (AbstractEdge<Node> edge : edgesFrom) {
            List<AbstractEdge<Node>> appended = append(current, edge);
            if (!seen.add(edge.destination())) {
                return appended;
            }
            List<AbstractEdge<Node>> cycle = findCycle(edge.destination(), appended, seen);
            if (!cycle.isEmpty()) {
                return cycle;
            }
        }
        return List.of();
    }

    private List<AbstractEdge<Node>> append(List<AbstractEdge<Node>> current, AbstractEdge<Node> next) {
        List<AbstractEdge<Node>> result = new ArrayList<>(current.size() + 1);
        result.addAll(current);
        result.add(next);
        return result;
    }
}
