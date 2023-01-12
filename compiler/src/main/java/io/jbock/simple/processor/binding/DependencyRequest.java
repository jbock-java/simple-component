package io.jbock.simple.processor.binding;

import javax.lang.model.element.VariableElement;

public record DependencyRequest(Key key, VariableElement requestElement) {
}
