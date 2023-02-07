package io.jbock.simple.processor.binding;

import io.jbock.simple.processor.util.Qualifiers;

import javax.lang.model.element.Element;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class DependencyRequest {

    private final Key key;
    private final Element requestingElement;
    private final Qualifiers qualifiers;

    public DependencyRequest(
            Key key,
            Element requestingElement,
            Qualifiers qualifiers) {
        this.key = key;
        this.requestingElement = requestingElement;
        this.qualifiers = qualifiers;
    }

    public Key key() {
        return key;
    }

    public Element requestingElement() {
        return requestingElement;
    }

    public Optional<Binding> binding() {
        return qualifiers.binding(key).map(Function.identity());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DependencyRequest) obj;
        return Objects.equals(this.key, that.key) &&
                Objects.equals(this.requestingElement, that.requestingElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, requestingElement);
    }

    @Override
    public String toString() {
        return "[" +
                "key=" + key + ", " +
                "requestingElement=" + requestingElement + ']';
    }
}
