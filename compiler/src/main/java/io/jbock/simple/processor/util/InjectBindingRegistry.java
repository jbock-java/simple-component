package io.jbock.simple.processor.util;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.TypeName;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Edge> getDependencies(DependencyRequest request) {
        List<Edge> acc = new ArrayList<>();
        InjectBinding injectBinding = bindingsByKey.get(request.key());
        if (injectBinding == null) {
            throw new ValidationFailure("Binding not found", request.requestElement());
        }
        addDependencies(acc, injectBinding);
        return acc;
    }

    private void addDependencies(
            List<Edge> acc,
            InjectBinding injectBinding) {
        List<DependencyRequest> dependencies = injectBinding.dependencies();
        for (DependencyRequest dependency : dependencies) {
            InjectBinding depBinding = bindingsByKey.get(dependency.key());
            if (depBinding == null) {
                throw new ValidationFailure("Binding not found", dependency.requestElement());
            }
            addDependencies(acc, depBinding);
            Edge edge = new Edge(depBinding, injectBinding);
            acc.add(edge);
        }
    }

}
