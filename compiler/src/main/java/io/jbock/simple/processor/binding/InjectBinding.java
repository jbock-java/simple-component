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

/**
 * This class represents either a {@code @Inject}-annotated constructor
 * or a {@code @Inject}-annotated static method.
 */
public final class InjectBinding extends Binding {

    private final ExecutableElement bindingElement;

    private final KeyFactory keyFactory;

    private static final List<String> PROVIDES_METHOD_COMMON_PREFIXES = List.of(
            "get",
            "provides",
            "provide",
            "create");

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

    private final Supplier<List<DependencyRequest>> requests = memoize(() -> element().getParameters().stream()
            .map(parameter -> new DependencyRequest(keyFactory().getKey(parameter), parameter, element()))
            .collect(Collectors.toList()));

    private InjectBinding(
            Key key,
            KeyFactory keyFactory,
            ExecutableElement bindingElement) {
        super(key);
        this.bindingElement = bindingElement;
        this.keyFactory = keyFactory;
    }

    static InjectBinding create(
            KeyFactory keyFactory,
            ExecutableElement m) {
        Key key = keyFactory.getKey(m);
        if (m.getKind() == ElementKind.CONSTRUCTOR) {
            if (key.qualifier().isPresent()) {
                throw new ValidationFailure("Constructors can't have qualifiers", m);
            }
        }
        return new InjectBinding(key, keyFactory, m);
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
    public List<DependencyRequest> requests() {
        return requests.get();
    }

    @Override
    public CodeBlock invocation(Function<Key, String> names) {
        CodeBlock params = requests().stream()
                .map(d -> CodeBlock.of("$L", names.apply(d.key())))
                .collect(CodeBlock.joining(", "));
        if (bindingElement.getKind() == ElementKind.CONSTRUCTOR) {
            return CodeBlock.of("new $T($L)", bindingElement.getEnclosingElement().asType(), params);
        } else {
            return CodeBlock.of("$T.$L($L)", bindingElement.getEnclosingElement().asType(), bindingElement.getSimpleName().toString(), params);
        }
    }

    @Override
    public String toString() {
        return "InjectBinding[" + key() + ']';
    }

    private KeyFactory keyFactory() {
        return keyFactory;
    }
}
