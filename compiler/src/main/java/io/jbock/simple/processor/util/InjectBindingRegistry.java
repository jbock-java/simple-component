package io.jbock.simple.processor.util;

import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;

import javax.lang.model.element.ExecutableElement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class InjectBindingRegistry {

    private final Qualifiers qualifiers;
    private final TypeTool tool;

    private final Map<Key, InjectBinding> bindingsByKey = new LinkedHashMap<>();

    public InjectBindingRegistry(Qualifiers qualifiers, TypeTool tool) {
        this.qualifiers = qualifiers;
        this.tool = tool;
    }

    public void registerConstructor(ExecutableElement element) {
        InjectBinding b = InjectBinding.createConstructor(qualifiers, tool, element);
        DuplicateBinding.check(b, bindingsByKey.put(b.key(), b));
    }

    public void registerFactoryMethod(ExecutableElement element) {
        InjectBinding b = InjectBinding.createMethod(qualifiers, tool, element);
        DuplicateBinding.check(b, bindingsByKey.put(b.key(), b));
    }

    public BindingRegistry createBindingRegistry(ComponentElement componentElement) {
        return BindingRegistry.create(bindingsByKey, componentElement);
    }

    public Collection<InjectBinding> allBindings() {
        return bindingsByKey.values();
    }
}
