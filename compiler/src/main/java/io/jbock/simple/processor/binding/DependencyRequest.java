package io.jbock.simple.processor.binding;

import javax.lang.model.element.Element;

public record DependencyRequest(Key key, Element requestingElement) {
}
