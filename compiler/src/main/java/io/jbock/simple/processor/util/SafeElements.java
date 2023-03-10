package io.jbock.simple.processor.util;

import io.jbock.simple.Inject;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SafeElements implements ClearableCache {

    private final Elements elements;

    private final Map<String, TypeElement> typeElementCache = new HashMap<>();

    @Inject
    public SafeElements(Elements elements) {
        this.elements = elements;
    }

    public TypeElement getTypeElement(String name) {
        TypeElement fromCache = typeElementCache.get(name);
        if (fromCache != null) {
            return fromCache;
        }
        if (typeElementCache.containsKey(name)) {
            return null;
        }
        TypeElement result = elements.getTypeElement(name);
        typeElementCache.put(name, result);
        return result;
    }

    public List<? extends Element> getAllMembers(TypeElement element) {
        return elements.getAllMembers(element);
    }

    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a) {
        return elements.getElementValuesWithDefaults(a);
    }

    @Override
    public void clearCache() {
        typeElementCache.clear();
    }
}
