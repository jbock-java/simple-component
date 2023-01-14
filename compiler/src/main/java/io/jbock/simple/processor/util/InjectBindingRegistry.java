package io.jbock.simple.processor.util;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.TypeName;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
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

    public BindingRegistry createBindingRegistry(ComponentElement componentElement) {
        return BindingRegistry.create(bindingsByKey, componentElement);
    }
}
