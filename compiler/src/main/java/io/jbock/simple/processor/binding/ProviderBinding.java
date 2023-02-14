package io.jbock.simple.processor.binding;

import io.jbock.javapoet.CodeBlock;
import io.jbock.simple.processor.util.ProviderType;

import javax.lang.model.element.Element;
import java.util.List;
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
    public List<DependencyRequest> dependencies() {
        return sourceBinding.dependencies();
    }

    @Override
    public CodeBlock invocation(Function<Key, String> names) {
        return CodeBlock.of("() -> $L", sourceBinding.invocation(names));
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
