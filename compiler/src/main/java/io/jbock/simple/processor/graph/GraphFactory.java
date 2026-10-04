package io.jbock.simple.processor.graph;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.InjectBindingFactory;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.KeyFactory;
import io.jbock.simple.processor.binding.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GraphFactory {

    private final KeyFactory keyFactory;
    private final InjectBindingFactory injectBindingFactory;
    private final Map<Key, Optional<? extends Node>> bindingCache = new HashMap<>();
    private final MissingBindingPrinter missingBindingPrinter;

    @Inject
    public GraphFactory(
            KeyFactory keyFactory,
            InjectBindingFactory injectBindingFactory,
            MissingBindingPrinter missingBindingPrinter) {
        this.keyFactory = keyFactory;
        this.injectBindingFactory = injectBindingFactory;
        this.missingBindingPrinter = missingBindingPrinter;
    }

    private Optional<? extends Node> getBindingMiss(Key key) {
        Node parameterBinding = keyFactory.parameterBindings().get(key);
        if (parameterBinding != null) {
            return Optional.of(parameterBinding);
        }
        InjectBinding providesBinding = keyFactory.providesBindings().get(key);
        if (providesBinding != null) {
            return Optional.of(providesBinding);
        }
        return injectBindingFactory.binding(key);
    }

    private Optional<? extends Node> getBinding(DependencyRequest request) {
        return bindingCache.computeIfAbsent(request.key(), this::getBindingMiss);
    }

    Graph<Node> getGraph(DependencyRequest request) {
        List<DependencyRequest> dependencyTrace = List.of(request);
        Node startNode = getBinding(request).orElseThrow(() -> missingBindingPrinter.fail(dependencyTrace));
        Set<AbstractEdge<Node>> edges = new LinkedHashSet<>();
        Set<Node> nodes = new LinkedHashSet<>();
        nodes.add(startNode);
        addDependencies(dependencyTrace, nodes, edges, startNode);
        return new Graph<>(edges, nodes);
    }

    private void addDependencies(
            List<DependencyRequest> trace,
            Set<Node> nodes,
            Set<AbstractEdge<Node>> edges,
            Node node) {
        for (DependencyRequest request : node.requests()) {
            List<DependencyRequest> dependencyTrace = new ArrayList<>(trace.size() + 1);
            dependencyTrace.addAll(trace);
            dependencyTrace.add(request);
            Node dependency = getBinding(request).orElseThrow(() -> missingBindingPrinter.fail(dependencyTrace));
            Edge edge = new Edge(dependency, node);
            edges.add(edge);
            if (!nodes.add(dependency)) {
                continue; // prevent stack overflow in invalid (cyclic) graph
            }
            addDependencies(dependencyTrace, nodes, edges, dependency);
        }
    }
}
