package io.jbock.simple.processor.binding;

import javax.lang.model.element.Element;

public final class DependencyRequest {

    private final Key key;
    private final Element requestingElement;

    public DependencyRequest(
            Key key,
            Element requestingElement) {
        this.key = key;
        this.requestingElement = requestingElement;
    }

    public Key key() {
        return key;
    }

    public Element requestingElement() {
        return requestingElement;
    }

    @Override
    public String toString() {
        return "[" +
                "key=" + key + ", " +
                "requestingElement=" + requestingElement + ']';
    }
}
