package io.jbock.simple.processor.writing;

import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;
import io.jbock.simple.processor.binding.ProviderBinding;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.FactoryElement;
import io.jbock.simple.processor.util.UniqueNameSet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Generator {

    private final ComponentImpl componentImpl;
    private final ComponentElement component;

    public Generator(
            ComponentImpl componentImpl,
            ComponentElement component) {
        this.componentImpl = componentImpl;
        this.component = component;
    }

    public TypeSpec generate(Set<Binding> sorted) {
        return componentImpl.generate(component, addNames(sorted));
    }

    Map<Key, NamedBinding> addNames(Set<Binding> sorted) {
        UniqueNameSet uniqueNameSet = new UniqueNameSet();
        List<ParameterBinding> parameterBindings = component.factoryElement()
                .map(FactoryElement::parameterBindings)
                .orElse(List.of());
        Map<Key, NamedBinding> result = new LinkedHashMap<>();
        for (ParameterBinding b : parameterBindings) {
            String name = b.parameter().getSimpleName().toString();
            result.put(b.key(), new NamedBinding(b, name, component.isComponentRequest(b)));
            uniqueNameSet.claim(name);
        }
        for (Binding binding : sorted) {
            if (binding instanceof InjectBinding) {
                InjectBinding b = (InjectBinding) binding;
                String name = uniqueNameSet.getUniqueName(b.suggestedVariableName());
                result.put(b.key(), new NamedBinding(b, name, component.isComponentRequest(b)));
            } else if (binding instanceof ProviderBinding) {
                ProviderBinding b = (ProviderBinding) binding;
                String name = uniqueNameSet.getUniqueName(b.sourceBinding().suggestedVariableName() + "Provider");
                result.put(b.key(), new NamedBinding(b, name, component.isComponentRequest(b)));
            }
        }
        return result;
    }
}
