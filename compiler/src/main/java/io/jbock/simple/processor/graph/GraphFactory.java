package io.jbock.simple.processor.graph;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBindingFactory;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ProviderBinding;
import io.jbock.simple.processor.util.ProviderType;
import io.jbock.simple.processor.util.TypeTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GraphFactory {

    private final ComponentElement component;
    private final TypeTool tool;
    private final InjectBindingFactory injectBindingFactory;
    private final Map<Key, Optional<Binding>> bindingCache = new HashMap<>();
    private final MissingBindingPrinter.Factory missingBindingPrinter;

    @Inject
    public GraphFactory(
            ComponentElement component,
            TypeTool tool,
            InjectBindingFactory injectBindingFactory,
            MissingBindingPrinter.Factory missingBindingPrinter) {
        this.component = component;
        this.tool = tool;
        this.injectBindingFactory = injectBindingFactory;
        this.missingBindingPrinter = missingBindingPrinter;
    }

    private Optional<Binding> getBinding(DependencyRequest request) {
        Key key = request.key();
        Optional<Binding> result = bindingCache.get(key);
        if (result != null) {
            return result;
        }
        result = component.parameterBinding(key)
                .or(() -> injectBindingFactory.binding(key))
                .or(() -> Optional.ofNullable(component.providesBindings().get(key)))
                .or(() -> providerBinding(key));
        if (result.isPresent()) {
            bindingCache.put(key, result);
        }
        return result;
    }

    private Optional<Binding> providerBinding(Key key) {
        Optional<ProviderType> providerType = tool.getProviderType(key.type());
        if (providerType.isEmpty()) {
            return Optional.empty();
        }
        ProviderType provider = providerType.orElseThrow();
        Key innerKey = key.changeType(provider.innerType());
        return component.parameterBinding(innerKey)
                .or(() -> injectBindingFactory.binding(innerKey))
                .or(() -> Optional.ofNullable(component.providesBindings().get(innerKey)))
                .map(b -> new ProviderBinding(key, b, provider));
    }

    Graph getGraph(DependencyRequest request) {
        List<DependencyRequest> dependencyTrace = List.of(request);
        Binding startNode = getBinding(request)
                .orElseThrow(() -> missingBindingPrinter.fail(dependencyTrace));
        Set<Edge> edges = new LinkedHashSet<>();
        Set<Binding> nodes = new LinkedHashSet<>();
        nodes.add(startNode);
        addDependencies(dependencyTrace, nodes, edges, startNode);
        return new Graph(edges, nodes);
    }

    private void addDependencies(
            List<DependencyRequest> trace,
            Set<Binding> nodes,
            Set<Edge> edges,
            Binding node) {
        for (DependencyRequest request : node.requests()) {
            List<DependencyRequest> dependencyTrace = new ArrayList<>(trace.size() + 1);
            dependencyTrace.addAll(trace);
            dependencyTrace.add(request);
            Binding dependency = getBinding(request)
                    .orElseThrow(() -> missingBindingPrinter.fail(dependencyTrace));
            Edge edge = new Edge(dependency, node);
            edges.add(edge);
            if (!nodes.add(dependency)) {
                continue; // prevent stack overflow in invalid (cyclic) graph
            }
            addDependencies(dependencyTrace, nodes, edges, dependency);
        }
    }
}
