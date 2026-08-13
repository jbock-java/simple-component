package io.jbock.simple.processor.binding;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.ClearableCache;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.lang.model.element.TypeElement;

public class InjectBindingCache implements ClearableCache {

    private final Map<TypeElement, Map<Key, InjectBinding>> injectBindingCache = new HashMap<>();

    @Inject
    public InjectBindingCache() {
    }

    Map<Key, InjectBinding> computeIfAbsent(
            TypeElement typeElement, 
            Function<TypeElement, Map<Key, InjectBinding>> f) {
        return injectBindingCache.computeIfAbsent(typeElement, f);    
    }


    @Override
    public void clearCache() {
        injectBindingCache.clear();
    }
}
