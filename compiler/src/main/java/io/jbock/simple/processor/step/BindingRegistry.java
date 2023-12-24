package io.jbock.simple.processor.step;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.KeyCache;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.HashMap;
import java.util.Map;

public class BindingRegistry {

    private final Map<Key, Element> bindings = new HashMap<>();
    private final KeyCache keyCache;

    @Inject
    public BindingRegistry(KeyCache keyCache) {
        this.keyCache = keyCache;
    }

    void register(ExecutableElement method) {
        Key key = keyCache.getKey(method);
        Element previous = bindings.put(key, method);
        if (previous != null && !previous.equals(method)) {
            throw new ValidationFailure("Duplicate binding for " + key, method);
        }
    }
}
