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
        Map<Key, ParameterBinding> m = new HashMap<>();
        for (ParameterBinding b : pBindings) {
            m.put(b.key(), b);
            if (bindingsByKey.containsKey(b.key())) {
                throw new ValidationFailure("Duplicate binding", b.parameter());
            }
        }
        return new BindingRegistry(bindingsByKey, m);
    }
    
    public Binding getBinding(DependencyRequest request) {
        InjectBinding injectBinding = bindingsByKey.get(request.key());
        if (injectBinding == null) {
            ParameterBinding parameterBinding = parameterBindings.get(request.key());
            if (parameterBinding == null) {
                throw new ValidationFailure("Binding not found", request.requestElement());
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
                return; // probably cyclic dependency
            }
            addDependencies(nodes, edges, dependency);
        }
    }
}
