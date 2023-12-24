package io.jbock.simple.processor.binding;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.TypeTool;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;

public final class InjectBindingFactory {

    private final InjectBindingCache injectBindingCache;
    private final TypeTool tool;
    private final KeyFactory keyFactory;
    private final InjectBindingScanner injectBindingScanner;

    @Inject
    public InjectBindingFactory(
            TypeTool tool,
            InjectBindingCache injectBindingCache,
            KeyFactory keyFactory,
            InjectBindingScanner injectBindingScanner) {
        this.tool = tool;
        this.injectBindingCache = injectBindingCache;
        this.keyFactory = keyFactory;
        this.injectBindingScanner = injectBindingScanner;
    }

    public Map<Key, InjectBinding> injectBindings(TypeElement typeElement) {
        return injectBindingCache.computeIfAbsent(typeElement, this::injectBindingsMiss);
    }

    private Map<Key, InjectBinding> injectBindingsMiss(TypeElement typeElement) {
        Map<Key, InjectBinding> result = new LinkedHashMap<>();
        List<ExecutableElement> methods = injectBindingScanner.scan(typeElement);
        for (ExecutableElement method : methods) {
            InjectBinding b = keyFactory.createBinding(method);
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
}
