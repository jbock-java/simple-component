package io.jbock.simple.processor.binding;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

public record DependencyRequest(
    Key key,
    Element requestingElement,
    ExecutableElement requestElement) {

    @Override
    public String toString() {
        return "[" +
                "key=" + key + ", " +
                "requestingElement=" + requestingElement + ']';
    }
}
