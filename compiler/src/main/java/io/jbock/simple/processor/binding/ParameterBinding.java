package io.jbock.simple.processor.binding;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.ParameterSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.function.Function;

public final class ParameterBinding extends Binding {

    private final Element element; // VariableElement or ExecutableElement (setter)

    private ParameterBinding(
            Key key,
            Element element) {
        super(key);
        this.element = element;
    }

    public static ParameterBinding create(VariableElement parameter, KeyFactory keyFactory) {
        Key key = keyFactory.getKey(parameter);
        return new ParameterBinding(key, parameter);
    }

    public static ParameterBinding create(ExecutableElement setter, KeyFactory keyFactory) {
        Key key = keyFactory.getKey(setter.getParameters().get(0));
        return new ParameterBinding(key, setter);
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
        return element;
    }

    @Override
    public List<DependencyRequest> requests() {
        return List.of();
    }

    @Override
    public String suggestedVariableName() {
        return element.getSimpleName().toString();
    }
}
