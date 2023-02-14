package io.jbock.simple.processor.binding;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;

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

public final class InjectBindingFactory {

    private final Map<TypeElement, Map<Key, InjectBinding>> injectBindingCache = new HashMap<>();

    private final TypeTool tool;
    private final KeyFactory keyFactory;

    @Inject
    public InjectBindingFactory(TypeTool tool, KeyFactory keyFactory) {
        this.tool = tool;
        this.keyFactory = keyFactory;
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
        for (ExecutableElement m : allMembers) {
            InjectBinding b = InjectBinding.create(keyFactory, m, this);
            InjectBinding previous = result.put(b.key(), b);
            if (previous != null) {
                throw new ValidationFailure("This binding clashes with " + previous.signature() + ", consider a (different) qualifier", b.element());
            }
            injectBindingCache.put(typeElement, result);
        }
        return result;
    }

    public Optional<InjectBinding> binding(Key key) {
        return tool.types().asElement(key.type()).flatMap(element -> {
            TypeElement typeElement = TYPE_ELEMENT_VISITOR.visit(element);
            if (typeElement == null) {
                return Optional.empty();
            }
            Map<Key, InjectBinding> m = injectBindings(typeElement);
            return Optional.ofNullable(m.get(key));
        });
    }
}
