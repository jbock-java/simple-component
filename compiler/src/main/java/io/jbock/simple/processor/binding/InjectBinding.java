package io.jbock.simple.processor.binding;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.ParameterizedTypeName;
import io.jbock.javapoet.TypeName;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.jbock.simple.processor.util.Suppliers.memoize;

public final class InjectBinding extends Binding {

    private final ExecutableElement bindingElement;

    private final Function<CodeBlock, CodeBlock> invokeExpression;

    private static final List<String> PROVIDES_METHOD_COMMON_PREFIXES = List.of(
            "get",
            "provides",
            "provide",
            "create");

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
        if (element().getAnnotation(Provides.class) != null) {
            return lowerFirst(removeMethodNamePrefix(element().getSimpleName().toString()));
        }
        TypeName typeName = key().typeName();
        if (typeName instanceof ParameterizedTypeName) {
            return lowerFirst(simpleTypeName((ParameterizedTypeName) typeName));
        }
        return lowerFirst(verySimpleTypeName(typeName.toString()));
    });

    private static String removeMethodNamePrefix(String s) {
        for (String p : PROVIDES_METHOD_COMMON_PREFIXES) {
            if (s.startsWith(p) && s.length() > p.length()) {
                return s.substring(p.length());
            }
        }
        return s;
    }

    static String simpleTypeName(ParameterizedTypeName type) {
        StringBuilder sb = new StringBuilder();
        sb.append(type.rawType.simpleName());
        for (TypeName typeName : type.typeArguments) {
            if (typeName instanceof ParameterizedTypeName) {
                sb.append(simpleTypeName((ParameterizedTypeName) typeName));
            } else {
                sb.append(verySimpleTypeName(typeName.toString()));
            }
        }
        return sb.toString();
    }

    static String verySimpleTypeName(String typeName) {
        int i = typeName.indexOf('<');
        if (i >= 0) {
            typeName = typeName.substring(0, i);
        }
        i = typeName.lastIndexOf('.');
        if (i >= 0) {
            typeName = typeName.substring(i + 1);
        }
        return typeName;
    }

    private static String lowerFirst(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private final List<DependencyRequest> dependencies;

    private InjectBinding(
            Key key,
            ExecutableElement bindingElement,
            Function<CodeBlock, CodeBlock> invokeExpression,
            List<DependencyRequest> dependencies) {
        super(key);
        this.bindingElement = bindingElement;
        this.invokeExpression = invokeExpression;
        this.dependencies = dependencies;
    }

    static InjectBinding create(
            KeyFactory keyFactory,
            ExecutableElement m,
            InjectBindingFactory injectBindingFactory) {
        Key key = keyFactory.getKey(m);
        Function<CodeBlock, CodeBlock> invokeExpression;
        if (m.getKind() == ElementKind.CONSTRUCTOR) {
            if (key.qualifier().isPresent()) {
                throw new ValidationFailure("Constructors can't have qualifiers", m);
            }
            invokeExpression = params -> CodeBlock.of("new $T($L)", m.getEnclosingElement().asType(), params);
        } else {
            invokeExpression = params -> CodeBlock.of("$T.$L($L)", m.getEnclosingElement().asType(), m.getSimpleName().toString(), params);
        }
        List<DependencyRequest> dependencies = m.getParameters().stream()
                .map(parameter -> new DependencyRequest(keyFactory.getKey(parameter), parameter, injectBindingFactory))
                .collect(Collectors.toList());
        return new InjectBinding(key, m, invokeExpression, dependencies);
    }

    @Override
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

    CodeBlock invokeExpression(CodeBlock params) {
        return invokeExpression.apply(params);
    }

    @Override
    public CodeBlock invocation(Function<Key, String> names) {
        return invokeExpression(dependencies().stream()
                .map(d -> CodeBlock.of("$L", names.apply(d.key())))
                .collect(CodeBlock.joining(", ")));
    }

    public String signature() {
        return signature.get();
    }

    @Override
    public String toString() {
        return "InjectBinding[" + key() + ']';
    }
}
