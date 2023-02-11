package io.jbock.simple.processor.binding;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.simple.processor.util.Qualifiers;
import io.jbock.simple.processor.util.Suppliers;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ParameterBinding extends Binding {

    private final VariableElement parameter;

    private final Supplier<ParameterSpec> parameterSpec = Suppliers.memoize(() ->
            ParameterSpec.builder(
                    TypeName.get(parameter().asType()),
                    parameter().getSimpleName().toString()).build());

    private ParameterBinding(
            Key key,
            VariableElement parameter) {
        super(key);
        this.parameter = parameter;
    }

    public static ParameterBinding create(VariableElement parameter, Qualifiers qualifiers) {
        Key key = Key.create(parameter.asType(), qualifiers.getQualifier(parameter));
        return new ParameterBinding(key, parameter);
    }

    public VariableElement parameter() {
        return parameter;
    }

    public ParameterSpec parameterSpec() {
        return parameterSpec.get();
    }

    @Override
    public CodeBlock invocation(Function<Key, String> names) {
        return CodeBlock.of("$N", parameterSpec());
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
    public List<DependencyRequest> dependencies() {
        return List.of();
    }

    @Override
    public String suggestedVariableName() {
        return parameter.getSimpleName().toString();
    }
}
