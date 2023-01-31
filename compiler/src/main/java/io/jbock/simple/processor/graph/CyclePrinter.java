package io.jbock.simple.processor.graph;

import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.util.ValidationFailure;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class CyclePrinter {

    private final Graph graph;
    private static final String INDENT = "    ";
    private static final String DOUBLE_INDENT = INDENT + INDENT;

    CyclePrinter(Graph graph) {
        this.graph = graph;
    }

    ValidationFailure fail() {
        Report report = cycleMessage();
        return new ValidationFailure(report.message, report.binding.element());
    }

    private static final class Report {
        final String message;
        final Binding binding;

        Report(String message, Binding binding) {
            this.message = message;
            this.binding = binding;
        }
    }

    Report cycleMessage() {
        for (Binding binding : graph.nodes()) {
            Optional<List<Edge>> cycle = findProperCycle(binding);
            if (cycle.isPresent()) {
                return new Report(cycleMessage(cycle.orElseThrow()), binding);
            }
        }
        throw new AssertionError("input didn't contain a cycle");
    }

    private String cycleMessage(List<Edge> cycle) {
        List<String> message = new ArrayList<>();
        message.add("Found a dependency cycle:");
        for (Edge edge : cycle) {
            Binding destination = edge.destination();
            if (destination instanceof InjectBinding) {
                InjectBinding b = (InjectBinding) destination;
                message.add(INDENT + edge.source().key().typeName() + " is injected at");
                message.add(DOUBLE_INDENT + b.signature());
            } else {
                throw new AssertionError("we should never get here");
            }
        }
        return String.join("\n", message);
    }

    private Optional<List<Edge>> findProperCycle(Binding node) {
        Set<Binding> seen = new LinkedHashSet<>();
        seen.add(node);
        List<Edge> cycle = findCycle(node, List.of(), seen);
        if (cycle.isEmpty()) {
            return Optional.empty();
        }
        if (!cycle.get(cycle.size() - 1).destination().equals(node)) {
            return Optional.empty();
        }
        return Optional.of(cycle);
    }

    private List<Edge> findCycle(
            Binding node,
            List<Edge> current,
            Set<Binding> seen) {
        List<Edge> edgesFrom = graph.edgesFrom(node);
        for (Edge edge : edgesFrom) {
            List<Edge> appended = append(current, edge);
            if (!seen.add(edge.destination())) {
                return appended;
            }
            List<Edge> cycle = findCycle(edge.destination(), appended, seen);
            if (!cycle.isEmpty()) {
                return cycle;
            }
        }
        return List.of();
    }

    private List<Edge> append(List<Edge> current, Edge next) {
        List<Edge> result = new ArrayList<>(current.size() + 1);
        result.addAll(current);
        result.add(next);
        return result;
    }
}
