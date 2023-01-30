package io.jbock.simple.processor.util;

import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jbock.simple.processor.util.Visitors.EXECUTABLE_ELEMENT_VISITOR;

public class Qualifiers {

    private final TypeTool tool;

    private final Map<TypeElement, Map<Key, InjectBinding>> injectBindingCache = new HashMap<>();

    public Qualifiers(TypeTool tool) {
        this.tool = tool;
    }

    public Optional<SimpleAnnotation> getQualifier(Element element) {
        List<SimpleAnnotation> qualifiers = element.getAnnotationMirrors().stream()
                .filter(this::hasQualifierAnnotation)
                .map(mirror -> SimpleAnnotation.create(mirror, tool.elements()))
                .collect(Collectors.toList());
        if (qualifiers.isEmpty()) {
            return Optional.empty();
        }
        if (qualifiers.size() == 1) {
            return Optional.of(qualifiers.get(0));
        }
        throw new ValidationFailure("Found more than one qualifier annotation", element);
    }

    private boolean hasQualifierAnnotation(AnnotationMirror mirror) {
        DeclaredType type = mirror.getAnnotationType();
        TypeElement element = Visitors.TYPE_ELEMENT_VISITOR.visit(type.asElement());
        return tool.hasQualifierAnnotation(element);
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
        InjectBinding b;
        for (ExecutableElement m : allMembers) {
            if (m.getKind() == ElementKind.CONSTRUCTOR) {
                b = InjectBinding.createConstructor(this, tool, m);
                if (b.key().qualifier().isPresent()) {
                    throw new ValidationFailure("Constructors can't have qualifiers, consider a static method", b.element());   
                }
            } else {
                b = InjectBinding.createMethod(this, tool, m);
            }
            InjectBinding previous = result.put(b.key(), b);
            if (previous != null) {
                throw new ValidationFailure("This clashes with " + previous.signature() + ", consider a (different) qualifier", b.element());
            }
            injectBindingCache.put(typeElement, result);
        }
        return result;
    }

    public TypeTool tool() {
        return tool;
    }
}
