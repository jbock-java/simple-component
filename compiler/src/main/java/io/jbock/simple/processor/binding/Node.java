package io.jbock.simple.processor.binding;

import com.palantir.javapoet.CodeBlock;
import io.jbock.simple.processor.writing.NamedBinding;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Map;

/**
 * A node in the dependency graph.
 *
 * <h1>Motivating example:
 *
 * <p>Consider an {@code @Inject}-annotated record.
 * The {@code @Inject} annotation makes the type of the record a node.
 * The types of the record's constructor parameters must also be nodes.
 * Each parameter declares an edge connecting the record's node
 * to its own type's node.
 */
public abstract sealed class Node
        permits InjectBinding, ParameterBinding {

    private final Key key;

    Node(Key key) {
        this.key = key;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node binding = (Node) o;
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
