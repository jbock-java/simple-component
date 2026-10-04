package io.jbock.simple.processor.binding;

import com.palantir.javapoet.CodeBlock;
import io.jbock.simple.processor.writing.NamedBinding;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;

/**
 * A node in the dependency graph.
 * For instance, consider an {@code @Inject}-annotated constructor.
 * The constructor itself is a "node".
 * From the constructor's perspective, each of its parameters is an "incoming edge"
 * which connects it to another "node".
 */
public abstract sealed class Binding
        permits InjectBinding, ParameterBinding, ProviderBinding {

    private final Key key;

    Binding(Key key) {
        this.key = key;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Binding binding = (Binding) o;
        return key.equals(binding.key);
    }

    @Override
    public final int hashCode() {
        return key.hashCode();
    }

    public final Key key() {
        return key;
    }

    public abstract Element element();

    public abstract List<DependencyRequest> requests();

    public abstract CodeBlock invocation(
            Map<Key, NamedBinding> bindings,
            boolean paramsAreFields);

    public abstract String suggestedVariableName();
}
