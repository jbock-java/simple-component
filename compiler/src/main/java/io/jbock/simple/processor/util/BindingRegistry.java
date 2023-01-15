package io.jbock.simple.processor.util;

import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;
import io.jbock.simple.processor.writing.Graph;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BindingRegistry {

    private final Map<Key, InjectBinding> bindingsByKey;
    private final Map<Key, ParameterBinding> parameterBindings;

    private BindingRegistry(
            Map<Key, InjectBinding> bindingsByKey,
            Map<Key, ParameterBinding> parameterBindings) {
        this.bindingsByKey = bindingsByKey;
        this.parameterBindings = parameterBindings;
    }

    static BindingRegistry create(
            Map<Key, InjectBinding> bindingsByKey,
            ComponentElement componentElement) {
        List<ParameterBinding> pBindings = componentElement.factoryElement()
                .map(FactoryElement::parameterBindings)
                .orElse(List.of());
        Map<Key, ParameterBinding> parameterBindings = new HashMap<>();
        for (ParameterBinding b : pBindings) {
            if (parameterBindings.put(b.key(), b) != null) {
                throw new ValidationFailure("Duplicate binding, consider adding a qualifier annotation", b.parameter());
            }
            if (bindingsByKey.containsKey(b.key())) {
                throw new ValidationFailure("Duplicate binding, consider adding a qualifier annotation", b.parameter());
            }
        }
        return new BindingRegistry(bindingsByKey, parameterBindings);
    }

    public Binding getBinding(DependencyRequest request) {
        InjectBinding injectBinding = bindingsByKey.get(request.key());
        if (injectBinding == null) {
            ParameterBinding parameterBinding = parameterBindings.get(request.key());
            if (parameterBinding == null) {
                throw new ValidationFailure("Binding not found", request.requestingElement());
            }
            return parameterBinding;
        }
        return injectBinding;
    }

    public Graph getDependencies(Binding startNode) {
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
                // probably cyclic dependency,
                // will be properly handled later on
                return;
            }
            addDependencies(nodes, edges, dependency);
        }
    }
}
