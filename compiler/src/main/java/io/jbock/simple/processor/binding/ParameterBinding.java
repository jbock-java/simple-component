package io.jbock.simple.processor.binding;

import io.jbock.javapoet.TypeName;

import javax.lang.model.element.VariableElement;

public final class ParameterBinding extends Binding {

    private final VariableElement parameter;

    private ParameterBinding(
            Key key,
            VariableElement parameter) {
        super(key);
        this.parameter = parameter;
    }

    public static ParameterBinding create(VariableElement parameter) {
        return new ParameterBinding(new Key(TypeName.get(parameter.asType())), parameter);
    }

    @Override
    public String toString() {
        return "ParameterBinding[" + "" + key() + ']';
    }
}
