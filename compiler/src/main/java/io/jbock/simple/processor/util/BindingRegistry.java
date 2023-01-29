package io.jbock.simple.processor.util;

import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;
import io.jbock.simple.processor.writing.Graph;

import javax.lang.model.element.VariableElement;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class BindingRegistry {

    private final Map<Key, ParameterBinding> parameterBindings;

    private BindingRegistry(
            Map<Key, ParameterBinding> parameterBindings) {
        this.parameterBindings = parameterBindings;
    }

    public static Function<ComponentElement, BindingRegistry> factory() {
        return BindingRegistry::create;
    }

    private static BindingRegistry create(ComponentElement componentElement) {
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
        return new BindingRegistry(parameterBindings);
    }

    public Binding getBinding(DependencyRequest request) {
        Optional<InjectBinding> injectBinding = request.binding();
        if (injectBinding.isEmpty()) {
            ParameterBinding parameterBinding = parameterBindings.get(request.key());
            if (parameterBinding == null) {
                throw new ValidationFailure("Binding not found", request.requestingElement());
            }
            return parameterBinding;
        }
        InjectBinding b = injectBinding.orElseThrow();
        DuplicateBinding.check(parameterBindings.get(request.key()), b);
        return b;
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
                continue; // prevent stack overflow in invalid (cyclic) graph
            }
            addDependencies(nodes, edges, dependency);
        }
    }
}
