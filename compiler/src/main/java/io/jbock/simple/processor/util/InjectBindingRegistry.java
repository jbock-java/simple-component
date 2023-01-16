package io.jbock.simple.processor.util;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.TypeName;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class InjectBindingRegistry {

    private final Qualifiers qualifiers;

    private final Map<Key, InjectBinding> bindingsByKey = new LinkedHashMap<>();

    public InjectBindingRegistry(Qualifiers qualifiers) {
        this.qualifiers = qualifiers;
    }

    public void registerConstructor(ExecutableElement element) {
        Element typeElement = element.getEnclosingElement();
        Key key = new Key(TypeName.get(typeElement.asType()), qualifiers.getQualifier(element));
        InjectBinding b = InjectBinding.create(key, element,
                params -> CodeBlock.of("new $T($L)", typeElement.asType(), params),
                qualifiers);
        DuplicateBinding.check(b, bindingsByKey.put(key, b));
    }

    public void registerFactoryMethod(ExecutableElement element) {
        TypeMirror returnType = element.getReturnType();
        Key key = new Key(TypeName.get(returnType), qualifiers.getQualifier(element));
        InjectBinding b = InjectBinding.create(key, element,
                params -> CodeBlock.of("$T.$L($L)", element.getEnclosingElement().asType(), element.getSimpleName().toString(), params),
                qualifiers);
        DuplicateBinding.check(b, bindingsByKey.put(key, b));
    }

    public BindingRegistry createBindingRegistry(ComponentElement componentElement) {
        return BindingRegistry.create(bindingsByKey, componentElement);
    }

    public Collection<InjectBinding> allBindings() {
        return bindingsByKey.values();
    }
}
