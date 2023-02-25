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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GraphFactory {

    private final ComponentElement component;
    private final TypeTool tool;
    private final InjectBindingFactory injectBindingFactory;
    private final Map<Key, Binding> bindingCache = new HashMap<>();
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

    private Binding getBinding(DependencyRequest request) {
        Key key = request.key();
        Binding result = bindingCache.get(key);
        if (result != null) {
            return result;
        }
        result = component.parameterBinding(key)
                .or(() -> injectBindingFactory.binding(key))
                .or(() -> Optional.ofNullable(component.providesBindings().get(key)))
                .or(() -> providerBinding(key))
                .orElseThrow(() -> missingBindingPrinter.fail(request));
        bindingCache.put(key, result);
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
        Binding startNode = getBinding(request);
        Set<Edge> edges = new LinkedHashSet<>();
        Set<Binding> nodes = new LinkedHashSet<>();
        nodes.add(startNode);
        addDependencies(nodes, edges, startNode);
        return new Graph(edges, nodes);
    }

    private void addDependencies(
            Set<Binding> nodes,
            Set<Edge> edges,
            Binding node) {
        for (DependencyRequest d : node.dependencies()) {
            Binding dependency = getBinding(d);
            Edge edge = new Edge(dependency, node);
            edges.add(edge);
            if (!nodes.add(dependency)) {
                continue; // prevent stack overflow in invalid (cyclic) graph
            }
            addDependencies(nodes, edges, dependency);
        }
    }
}
