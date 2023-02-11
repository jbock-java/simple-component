package io.jbock.simple.processor.binding;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.SimpleAnnotation;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.util.Visitors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jbock.simple.processor.util.Visitors.EXECUTABLE_ELEMENT_VISITOR;
import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;

public class KeyFactory {

    private final TypeTool tool;

    private final Map<TypeElement, Map<Key, InjectBinding>> injectBindingCache = new HashMap<>();

    @Inject
    public KeyFactory(TypeTool tool) {
        this.tool = tool;
    }

    private Optional<SimpleAnnotation> getQualifier(Element element) {
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

    public Key getKey(ExecutableElement element) {
        TypeMirror returnType;
        if (element.getKind() == ElementKind.CONSTRUCTOR) {
            returnType = element.getEnclosingElement().asType();
        } else {
            returnType = element.getReturnType();
        }
        return Key.create(returnType, getQualifier(element));
    }

    public Key getKey(VariableElement parameter) {
        return Key.create(parameter.asType(), getQualifier(parameter));
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
                b = InjectBinding.createConstructor(this, m);
                if (b.key().qualifier().isPresent()) {
                    throw new ValidationFailure("Constructors can't have qualifiers", b.element());
                }
            } else {
                b = InjectBinding.createMethod(this, m);
            }
            InjectBinding previous = result.put(b.key(), b);
            if (previous != null) {
                throw new ValidationFailure("This binding clashes with " + previous.signature() + ", consider a (different) qualifier", b.element());
            }
            injectBindingCache.put(typeElement, result);
        }
        return result;
    }

    public TypeTool tool() {
        return tool;
    }

    public Optional<Element> keyElement(Key key) {
        return tool().types().asElement(key.type());
    }

    public Optional<InjectBinding> binding(Key key) {
        return keyElement(key).flatMap(element -> binding(key, element));
    }

    public Optional<InjectBinding> binding(Key key, Element element) {
        TypeElement typeElement = TYPE_ELEMENT_VISITOR.visit(element);
        if (typeElement == null) {
            return Optional.empty();
        }
        Map<Key, InjectBinding> m = injectBindings(typeElement);
        return Optional.ofNullable(m.get(key));
    }
}
