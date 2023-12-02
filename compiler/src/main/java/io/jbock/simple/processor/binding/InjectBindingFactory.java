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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jbock.simple.processor.util.Visitors.EXECUTABLE_ELEMENT_VISITOR;
import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;

public final class InjectBindingFactory implements ClearableCache {

    private final Map<TypeElement, Map<Key, InjectBinding>> injectBindingCache = new HashMap<>();

    private final TypeTool tool;
    private final KeyFactory keyFactory;
    private final InjectBinding.Factory injectBindingFactory;

    @Inject
    public InjectBindingFactory(
            TypeTool tool,
            KeyFactory keyFactory,
            InjectBinding.Factory injectBindingFactory) {
        this.tool = tool;
        this.keyFactory = keyFactory;
        this.injectBindingFactory = injectBindingFactory;
    }

    public Map<Key, InjectBinding> injectBindings(TypeElement typeElement) {
        Map<Key, InjectBinding> result = injectBindingCache.get(typeElement);
        if (result != null) {
            return result;
        }
        List<? extends ExecutableElement> allMembers = tool.elements().getAllMembers(typeElement).stream()
                .filter(tool::hasInjectAnnotation)
                .map(EXECUTABLE_ELEMENT_VISITOR::visit)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (allMembers.isEmpty()) {
            return Map.of();
        }
        result = new LinkedHashMap<>();
        for (ExecutableElement method : allMembers) {
            InjectBinding b = injectBindingFactory.create(method);
            result.put(b.key(), b); // duplicates handled elsewhere
        }
        injectBindingCache.put(typeElement, result);
        return result;
    }

    public Optional<Binding> binding(Key key) {
        return tool.types().asElement(key.type()).flatMap(element -> {
            TypeElement typeElement = TYPE_ELEMENT_VISITOR.visit(element);
            if (typeElement == null) {
                return Optional.empty();
            }
            Map<Key, InjectBinding> m = injectBindings(typeElement);
            return Optional.ofNullable(m.get(key));
        });
    }

    @Override
    public void clearCache() {
        injectBindingCache.clear();
    }
}
