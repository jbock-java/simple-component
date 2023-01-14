package io.jbock.simple.processor.binding;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;

public final class InjectBinding extends Binding {

    private final ExecutableElement bindingElement;

    private final Function<CodeBlock, CodeBlock> invokeExpression;

    private final Supplier<String> signature = memoize(() -> CodeBlock.of(
            "$T($L)",
            key().typeName(),
            dependencies().stream()
                    .map(d -> CodeBlock.of("$L", d.requestElement().getSimpleName().toString()))
                    .collect(CodeBlock.joining(", "))).toString());

    private final Supplier<String> suggestedVariableName = memoize(() -> {
        String[] tokens = key().typeName().toString().split("[.]");
        String simpleName = tokens[tokens.length - 1];
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    });

    private final Supplier<List<DependencyRequest>> dependencies = memoize(() -> {
        List<DependencyRequest> result = new ArrayList<>();
        for (VariableElement parameter : bindingElement().getParameters()) {
            result.add(new DependencyRequest(new Key(TypeName.get(parameter.asType())), parameter));
        }
        return result;
    });

    public InjectBinding(
            Key key,
            ExecutableElement bindingElement,
            Function<CodeBlock, CodeBlock> invokeExpression) {
        super(key);
        this.bindingElement = bindingElement;
        this.invokeExpression = invokeExpression;
    }

    public String suggestedVariableName() {
        return suggestedVariableName.get();
    }

    public ExecutableElement bindingElement() {
        return bindingElement;
    }

    @Override
    public Element element() {
        return bindingElement;
    }

    @Override
    public List<DependencyRequest> dependencies() {
        return dependencies.get();
    }

    public CodeBlock invokeExpression(CodeBlock params) {
        return invokeExpression.apply(params);
    }

    public String signature() {
        return signature.get();
    }

    @Override
    public String toString() {
        return "InjectBinding[" + "" + key() + ']';
    }
}
