package io.jbock.simple.processor.binding;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

public final class DependencyRequest {

    private final Key key;
    private final Element requestingElement;
    private final ExecutableElement requestElement;

    public DependencyRequest(
            Key key,
            Element requestingElement, 
            ExecutableElement requestElement) {
        this.key = key;
        this.requestingElement = requestingElement;
        this.requestElement = requestElement;
    }

    public Key key() {
        return key;
    }

    public Element requestingElement() {
        return requestingElement;
    }

    public ExecutableElement requestElement() {
        return requestElement;
    }

    @Override
    public String toString() {
        return "[" +
                "key=" + key + ", " +
                "requestingElement=" + requestingElement + ']';
    }
}
