package io.jbock.simple.processor.graph;

import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.InjectBindingFactory;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;
import io.jbock.simple.processor.binding.ProviderBinding;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.FactoryElement;
import io.jbock.simple.processor.util.ProviderType;
import io.jbock.simple.processor.binding.KeyFactory;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.lang.model.element.VariableElement;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GraphFactory {

    private final Map<Key, ParameterBinding> parameterBindings;
    private final Map<Key, InjectBinding> providers;
    private final KeyFactory keyFactory;
    private final InjectBindingFactory injectBindingFactory;

    private GraphFactory(
            Map<Key, ParameterBinding> parameterBindings,
            Map<Key, InjectBinding> providers,
            KeyFactory keyFactory, 
            InjectBindingFactory injectBindingFactory) {
        this.parameterBindings = parameterBindings;
        this.providers = providers;
        this.keyFactory = keyFactory;
        this.injectBindingFactory = injectBindingFactory;
    }

    public static GraphFactory create(
            ComponentElement componentElement,
            InjectBindingFactory injectBindingFactory) {
        List<ParameterBinding> pBindings = componentElement.factoryElement()
                .map(FactoryElement::parameterBindings)
                .orElse(List.of());
        Map<Key, ParameterBinding> parameterBindings = new HashMap<>();
        for (ParameterBinding b : pBindings) {
            ParameterBinding previousBinding = parameterBindings.put(b.key(), b);
            if (previousBinding != null) {
                VariableElement p = previousBinding.parameter();
                throw new ValidationFailure("The binding is in conflict with another parameter: " +
                        p.asType() + ' ' + p.getSimpleName(), b.parameter());
            }
        }
        return new GraphFactory(parameterBindings, componentElement.providers(), componentElement.qualifiers(), injectBindingFactory);
    }

    private Binding getBinding(DependencyRequest request) {
        Key key = request.key();
        ParameterBinding parameterBinding = parameterBindings.get(key);
        if (parameterBinding != null) {
            return parameterBinding; // takes precedence
        }
        return request.binding()
                .or(() -> Optional.ofNullable(providers.get(key)))
                .or(() -> {
                    Optional<ProviderType> providerType = keyFactory.tool().getProviderType(request.key().type());
                    if (providerType.isEmpty()) {
                        return Optional.empty();
                    }
                    ProviderType provider = providerType.orElseThrow();
                    Key innerKey = key.changeType(provider.innerType());
                    return injectBindingFactory.binding(innerKey)
                            .or(() -> Optional.ofNullable(providers.get(innerKey)))
                            .map(b -> new ProviderBinding(key, b, provider));
                })
                .orElseThrow(() -> new ValidationFailure("Binding not found", request.requestingElement()));
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
