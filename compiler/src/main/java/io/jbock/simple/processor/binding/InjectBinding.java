package io.jbock.simple.processor.binding;

import javax.lang.model.element.ExecutableElement;

public record InjectBinding(
        Key key,
        ExecutableElement bindingElement) implements Binding {
}
