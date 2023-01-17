package io.jbock.simple.processor.binding;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.simple.processor.util.Qualifiers;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.Visitors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;
import static java.util.Objects.requireNonNull;

public final class InjectBinding extends Binding {

    private final ExecutableElement bindingElement;

    private final Function<CodeBlock, CodeBlock> invokeExpression;

    private final Supplier<TypeElement> enclosingElement = memoize(() -> {
        Element el = element().getEnclosingElement();
        return requireNonNull(Visitors.TYPE_ELEMENT_VISITOR.visit(el));
    });

    private final Supplier<String> accessMethodName = memoize(() -> {
        if (element().getKind() == ElementKind.CONSTRUCTOR) {
            return "create";
        }
        return element().getSimpleName().toString();
    });

    private final Supplier<ClassName> accessClassName = memoize(() -> {
        Element enclosing = element().getEnclosingElement();
        ClassName className = ClassName.get(Visitors.TYPE_ELEMENT_VISITOR.visit(enclosing));
        return className.topLevelClassName()
                .peerClass(String.join("_", className.simpleNames()) + "_Access");
    });

    private final Supplier<String> signature = memoize(() -> {
        CodeBlock deps = dependencies().stream()
                .map(d -> CodeBlock.of("$L", d.requestingElement().getSimpleName().toString()))
                .collect(CodeBlock.joining(", "));
        if (element().getKind() == ElementKind.CONSTRUCTOR) {
            return CodeBlock.of("$T($L)", key().typeName(), deps).toString();
        }
        return CodeBlock.of("$T.$L($L)",
                element().getEnclosingElement().asType(), element().getSimpleName(), deps).toString();
    });

    private final Supplier<String> suggestedVariableName = memoize(() -> {
        String[] tokens = key().typeName().toString().split("[.]");
        String simpleName = tokens[tokens.length - 1];
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    });

    private final List<DependencyRequest> dependencies;

    public InjectBinding(
            Key key,
            ExecutableElement bindingElement,
            Function<CodeBlock, CodeBlock> invokeExpression,
            List<DependencyRequest> dependencies) {
        super(key);
        this.bindingElement = bindingElement;
        this.invokeExpression = invokeExpression;
        this.dependencies = dependencies;
    }

    public static InjectBinding create(
            Key key,
            ExecutableElement bindingElement,
            Function<CodeBlock, CodeBlock> invokeExpression,
            Qualifiers qualifiers,
            TypeTool tool) {
        List<DependencyRequest> dependencies = new ArrayList<>();
        for (VariableElement parameter : bindingElement.getParameters()) {
            dependencies.add(new DependencyRequest(Key.create(parameter.asType(),
                    qualifiers.getQualifier(parameter)), parameter, qualifiers, tool));
        }
        return new InjectBinding(key, bindingElement, invokeExpression, dependencies);
    }

    public static InjectBinding createConstructor(
            Qualifiers qualifiers,
            TypeTool tool,
            ExecutableElement element) {
        Element typeElement = element.getEnclosingElement();
        Key key = Key.create(typeElement.asType(), qualifiers.getQualifier(element));
        return create(key, element,
                params -> CodeBlock.of("new $T($L)", typeElement.asType(), params),
                qualifiers, tool);
    }

    public static InjectBinding createMethod(
            Qualifiers qualifiers,
            TypeTool tool,
            ExecutableElement element) {
        TypeMirror returnType = element.getReturnType();
        Key key = Key.create(returnType, qualifiers.getQualifier(element));
        return InjectBinding.create(key, element,
                params -> CodeBlock.of("$T.$L($L)", element.getEnclosingElement().asType(), element.getSimpleName().toString(), params),
                qualifiers, tool);
    }

    public String suggestedVariableName() {
        return suggestedVariableName.get();
    }

    @Override
    public ExecutableElement element() {
        return bindingElement;
    }

    @Override
    public List<DependencyRequest> dependencies() {
        return dependencies;
    }

    public CodeBlock invokeExpression(CodeBlock params) {
        return invokeExpression.apply(params);
    }

    public ClassName accessClassName() {
        return accessClassName.get();
    }

    public String accessMethodName() {
        return accessMethodName.get();
    }

    public TypeElement enclosingElement() {
        return enclosingElement.get();
    }

    public String signature() {
        return signature.get();
    }

    @Override
    public String toString() {
        return "InjectBinding[" + "" + key() + ']';
    }
}
