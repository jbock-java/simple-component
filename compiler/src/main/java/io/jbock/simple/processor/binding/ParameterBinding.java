package io.jbock.simple.processor.binding;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.simple.processor.writing.NamedBinding;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ParameterBinding extends Binding {

    private final Element element; // VariableElement or ExecutableElement (setter)
    private final String suggestedVariableName;

    private ParameterBinding(
            Key key,
            Element element,
            String suggestedVariableName) {
        super(key);
        this.element = element;
        this.suggestedVariableName = suggestedVariableName;
    }

    public static ParameterBinding create(VariableElement parameter, KeyFactory keyFactory) {
        Key key = keyFactory.getKey(parameter);
        return new ParameterBinding(key, parameter, parameter.getSimpleName().toString());
    }

    public static ParameterBinding create(ExecutableElement setter, KeyFactory keyFactory) {
        VariableElement parameter = setter.getParameters().get(0);
        Key key = keyFactory.getKey(parameter);
        return new ParameterBinding(key, setter, parameter.getSimpleName().toString());
    }

    @Override
    public CodeBlock invocation(
            Function<Key, ParameterSpec> names, 
            boolean thisForNames,
            Map<Key, NamedBinding> bindings) {
        ParameterSpec param = names.apply(key());
        return thisForNames ? CodeBlock.of("this.$N", param) : CodeBlock.of("$N", param);
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
        return suggestedVariableName;
    }
}
