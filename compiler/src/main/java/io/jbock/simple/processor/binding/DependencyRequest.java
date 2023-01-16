package io.jbock.simple.processor.binding;

import io.jbock.simple.processor.util.TypeTool;

import javax.lang.model.element.Element;
import java.util.Objects;

public final class DependencyRequest {

    private final Key key;
    private final Element requestingElement;
    private final TypeTool tool;

    public DependencyRequest(
            Key key,
            Element requestingElement,
            TypeTool tool) {
        this.key = key;
        this.requestingElement = requestingElement;
        this.tool = tool;
    }

    public Key key() {
        return key;
    }

    public Element requestingElement() {
        return requestingElement;
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
