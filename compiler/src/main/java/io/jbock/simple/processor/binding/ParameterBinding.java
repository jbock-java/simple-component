package io.jbock.simple.processor.binding;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.ParameterSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.function.Function;

public final class ParameterBinding extends Binding {

    private final VariableElement parameter;

    private ParameterBinding(
            Key key,
            VariableElement parameter) {
        super(key);
        this.parameter = parameter;
    }

    public static ParameterBinding create(VariableElement parameter, KeyFactory keyFactory) {
        Key key = keyFactory.getKey(parameter);
        return new ParameterBinding(key, parameter);
    }

    public VariableElement parameter() {
        return parameter;
    }

    @Override
    public CodeBlock invocation(Function<Key, ParameterSpec> names) {
        return CodeBlock.of("$N", names.apply(key()));
    }

    @Override
    public String toString() {
        return "ParameterBinding[" + "" + key() + ']';
    }

    @Override
    public Element element() {
        return parameter;
    }

    @Override
    public List<DependencyRequest> requests() {
        return List.of();
    }

    @Override
    public String suggestedVariableName() {
        return parameter.getSimpleName().toString();
    }
}
