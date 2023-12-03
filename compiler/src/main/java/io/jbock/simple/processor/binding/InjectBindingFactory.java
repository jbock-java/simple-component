package io.jbock.simple.processor.binding;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.ClearableCache;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.Util;

import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.jbock.simple.processor.util.Visitors.EXECUTABLE_ELEMENT_VISITOR;
import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;

public final class InjectBindingFactory implements ClearableCache {

    private final Map<TypeElement, Map<Key, InjectBinding>> injectBindingCache = new HashMap<>();

    private final TypeTool tool;
    private final InjectBinding.Factory injectBindingFactory;

    @Inject
    public InjectBindingFactory(
            TypeTool tool,
            InjectBinding.Factory injectBindingFactory) {
        this.tool = tool;
        this.injectBindingFactory = injectBindingFactory;
    }

    public Map<Key, InjectBinding> injectBindings(TypeElement typeElement) {
        return injectBindingCache.computeIfAbsent(typeElement, this::injectBindingsMiss);
    }

    private Map<Key, InjectBinding> injectBindingsMiss(TypeElement typeElement) {
        Map<Key, InjectBinding> result = new LinkedHashMap<>();
        for (TypeElement element : Util.getWithEnclosing(typeElement)) {
            tool.elements().getAllMembers(element).stream()
                    .filter(tool::hasInjectAnnotation)
                    .map(EXECUTABLE_ELEMENT_VISITOR::visit)
                    .filter(Objects::nonNull)
                    .map(injectBindingFactory::create)
                    .forEach(b -> result.put(b.key(), b) /* duplicates handled elsewhere */);
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
