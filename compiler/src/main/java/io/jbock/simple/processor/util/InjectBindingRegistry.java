package io.jbock.simple.processor.util;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.TypeName;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.writing.Graph;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class InjectBindingRegistry {

    private final Map<Key, InjectBinding> bindingsByKey = new HashMap<>();

    public void registerConstructor(ExecutableElement element) {
        Element typeElement = element.getEnclosingElement();
        Key key = new Key(TypeName.get(typeElement.asType()));
        InjectBinding previousValue = bindingsByKey.put(key, new InjectBinding(key, element,
                params -> CodeBlock.of("new $T($L)", typeElement.asType(), params)));
        if (previousValue != null) {
            throw new ValidationFailure("Duplicate binding", element);
        }
    }

    public void registerFactoryMethod(ExecutableElement element) {
        TypeMirror returnType = element.getReturnType();
        Key key = new Key(TypeName.get(returnType));
        InjectBinding previousValue = bindingsByKey.put(key, new InjectBinding(key, element,
                params -> CodeBlock.of("$T.$L($L)", element.getEnclosingElement().asType(), element.getSimpleName().toString(), params)));
        if (previousValue != null) {
            throw new ValidationFailure("Duplicate binding", element);
        }
    }

    public InjectBinding getBinding(DependencyRequest request) {
        InjectBinding injectBinding = bindingsByKey.get(request.key());
        if (injectBinding == null) {
            throw new ValidationFailure("Binding not found", request.requestElement());
        }
        return injectBinding;
    }

    public Graph getDependencies(InjectBinding startNode) {
        Set<Edge> edges = new LinkedHashSet<>();
        Set<InjectBinding> nodes = new LinkedHashSet<>();
        nodes.add(startNode);
        addDependencies(nodes, edges, startNode);
        return new Graph(edges, nodes);
    }

    private void addDependencies(
            Set<InjectBinding> nodes,
            Set<Edge> edges,
            InjectBinding node) {
        for (DependencyRequest d : node.dependencies()) {
            InjectBinding dependency = getBinding(d);
            Edge edge = new Edge(dependency, node);
            edges.add(edge);
            if (!nodes.add(dependency)) {
                return; // probably cyclic dependency
            }
            addDependencies(nodes, edges, dependency);
        }
    }
}
