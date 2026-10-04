package io.jbock.simple.processor.binding;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.ParameterSpec;
import io.jbock.simple.processor.writing.NamedBinding;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Map;

/**
 * This class represents an instance that gets injected
 * via {@linkplain io.jbock.simple.Component.Factory Component.Factory} or
 * {@linkplain io.jbock.simple.Component.Builder Component.Builder}.
 */
public final class ParameterBinding extends Node {

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

    public static ParameterBinding create(
            VariableElement parameter,
            KeyFactory keyFactory) {
        Key key = keyFactory.getKey(parameter);
        return new ParameterBinding(key, parameter, parameter.getSimpleName().toString());
    }

    public static ParameterBinding create(
            ExecutableElement setter,
            KeyFactory keyFactory) {
        VariableElement parameter = setter.getParameters().getFirst();
        Key key = keyFactory.getKey(parameter);
        return new ParameterBinding(key, setter, parameter.getSimpleName().toString());
    }

    @Override
    public CodeBlock invocation(
            Map<Key, NamedBinding> bindings,
            boolean paramsAreFields) {
        ParameterSpec param = bindings.get(key()).parameter();
        CodeBlock result;
        if (paramsAreFields) {
            result = CodeBlock.of("this.$N", param);
        } else {
            result = CodeBlock.of("$N", param);
        }
        return result;
    }

    @Override
    public String toString() {
        return "ParameterBinding[" + key() + ']';
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
