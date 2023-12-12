package io.jbock.simple.processor.binding;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.simple.processor.util.ProviderType;
import io.jbock.simple.processor.writing.NamedBinding;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProviderBinding extends Binding {

    private final Binding sourceBinding;
    private final ProviderType providerType;

    public ProviderBinding(
            Key key,
            Binding sourceBinding,
            ProviderType providerType) {
        super(key);
        this.sourceBinding = sourceBinding;
        this.providerType = providerType;
    }

    @Override
    public Element element() {
        return sourceBinding.element();
    }

    @Override
    public List<DependencyRequest> requests() {
        return sourceBinding.requests();
    }

    @Override
    public CodeBlock invocation(
            Function<Key, ParameterSpec> names,
            Map<Key, NamedBinding> bindings,
            boolean paramsAreFields) {
        return CodeBlock.of("() -> $L", sourceBinding.invocation(names, bindings, paramsAreFields));
    }

    public Binding sourceBinding() {
        return sourceBinding;
    }

    @Override
    public String suggestedVariableName() {
        return sourceBinding.suggestedVariableName() + "Provider";
    }

    @Override
    public String toString() {
        return providerType.kind().className() + "<" + sourceBinding.key() + ">";
    }
}
