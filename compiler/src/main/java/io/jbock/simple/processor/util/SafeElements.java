package io.jbock.simple.processor.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Map;
import java.util.Optional;

/**
 * A wrapper around {@link Elements} where none of the methods can return {@code null}.
 */
public class SafeElements {

    private final Elements elements;

    public SafeElements(Elements elements) {
        this.elements = elements;
    }

    public Optional<TypeElement> getTypeElement(String name) {
        return Optional.ofNullable(elements.getTypeElement(name));
    }

    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a) {
        return elements.getElementValuesWithDefaults(a);
    }
}
