package io.jbock.simple.processor.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A wrapper around {@link Elements} where none of the methods can return {@code null}.
 */
public class SafeElements {

    private final Elements elements;

    private final Map<String, Optional<TypeElement>> typeElementCache = new HashMap<>();

    public SafeElements(Elements elements) {
        this.elements = elements;
    }

    public Optional<TypeElement> getTypeElement(String name) {
        Optional<TypeElement> fromCache = typeElementCache.get(name);
        if (fromCache != null) {
            return fromCache;
        }
        Optional<TypeElement> result = Optional.ofNullable(elements.getTypeElement(name));
        typeElementCache.put(name, result);
        return result;
    }

    public List<? extends Element> getAllMembers(TypeElement element) {
        return elements.getAllMembers(element);
    }

    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a) {
        return elements.getElementValuesWithDefaults(a);
    }
}
