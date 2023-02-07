package io.jbock.simple.processor.binding;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.ParameterizedTypeName;
import io.jbock.simple.processor.util.ProviderType;
import io.jbock.simple.processor.util.ProviderType.ProviderKind;

import javax.lang.model.element.Element;
import java.util.List;

public class ProviderBinding extends Binding {

    private final InjectBinding sourceBinding;
    private final ProviderType providerType;

    public ProviderBinding(
            Key key,
            InjectBinding sourceBinding,
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

    public ProviderKind kind() {
        return providerType.kind();
    }

    public InjectBinding sourceBinding() {
        return sourceBinding;
    }

    public ParameterizedTypeName providerType() {
        ClassName frameworkClass = ClassName.bestGuess(kind().className());
        return ParameterizedTypeName.get(frameworkClass, key().typeName());
    }

    @Override
    public String toString() {
        return providerType.kind().className() + "<" + sourceBinding.key() + ">";
    }
}
