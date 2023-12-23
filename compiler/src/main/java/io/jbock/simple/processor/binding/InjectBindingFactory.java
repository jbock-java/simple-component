package io.jbock.simple.processor.binding;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.ClearableCache;
import io.jbock.simple.processor.util.TypeTool;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;

public final class InjectBindingFactory implements ClearableCache {

    private final Map<TypeElement, Map<Key, InjectBinding>> injectBindingCache = new HashMap<>();

    private final TypeTool tool;
    private final InjectBinding.Factory injectBindingFactory;
    private final InjectBindingScanner injectBindingScanner;

    @Inject
    public InjectBindingFactory(
            TypeTool tool,
            InjectBinding.Factory injectBindingFactory,
            InjectBindingScanner injectBindingScanner) {
        this.tool = tool;
        this.injectBindingFactory = injectBindingFactory;
        this.injectBindingScanner = injectBindingScanner;
    }

    public Map<Key, InjectBinding> injectBindings(TypeElement typeElement) {
        return injectBindingCache.computeIfAbsent(typeElement, this::injectBindingsMiss);
    }

    private Map<Key, InjectBinding> injectBindingsMiss(TypeElement typeElement) {
        Map<Key, InjectBinding> result = new LinkedHashMap<>();
        List<ExecutableElement> methods = injectBindingScanner.scan(typeElement);
        for (ExecutableElement method : methods) {
            InjectBinding b = injectBindingFactory.create(method);
            result.put(b.key(), b);
        }
        return result;
    }

    public Optional<Binding> binding(Key key) {
        return tool.types().asElement(key.type())
                .map(TYPE_ELEMENT_VISITOR::visit)
                .flatMap(typeElement -> {
                    Map<Key, InjectBinding> m = injectBindings(typeElement);
                    return Optional.ofNullable(m.get(key));
                });
    }

    @Override
    public void clearCache() {
        injectBindingCache.clear();
    }
}
