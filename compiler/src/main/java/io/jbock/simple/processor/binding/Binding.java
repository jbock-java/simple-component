package io.jbock.simple.processor.binding;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Objects;

/**
 * A node in the dependency graph. For instance, an inject-annotated constructor.
 * Every constructor argument is an "incoming edge".
 * Parameters to the component factory are nodes without any incoming edges.
 */
public abstract class Binding { // permits InjectBinding, ParameterBinding

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
        return Objects.hash(key);
    }

    public final Key key() {
        return key;
    }

    public abstract Element element();

    public abstract List<DependencyRequest> dependencies();
}
