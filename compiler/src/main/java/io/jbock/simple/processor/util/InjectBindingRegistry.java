package io.jbock.simple.processor.util;

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
        register(key, element);
    }

    public void registerFactoryMethod(ExecutableElement element) {
        TypeMirror returnType = element.getReturnType();
        Key key = new Key(TypeName.get(returnType));
        register(key, element);
    }

    private void register(Key key, ExecutableElement element) {
        bindingsByKey.put(key, new InjectBinding(key, element));
    }
}
