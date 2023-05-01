package io.jbock.simple.processor.binding;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.ClearableCache;
import io.jbock.simple.processor.util.SimpleAnnotation;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;

public class KeyFactory implements ClearableCache {

    private final TypeTool tool;
    private final Map<Element, Key> keyCache = new HashMap<>();

    @Inject
    public KeyFactory(TypeTool tool) {
        this.tool = tool;
    }

    private Optional<SimpleAnnotation> getQualifier(Element element) {
        List<SimpleAnnotation> qualifiers = element.getAnnotationMirrors().stream()
                .filter(this::hasQualifierAnnotation)
                .map(mirror -> SimpleAnnotation.create(mirror, tool.elements(), tool.types()))
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
        Key key = keyCache.get(element);
        if (key != null) {
            return key;
        }
        TypeMirror returnType;
        if (element.getKind() == CONSTRUCTOR) {
            returnType = element.getEnclosingElement().asType();
        } else {
            returnType = element.getReturnType();
        }
        key = Key.create(returnType, getQualifier(element));
        keyCache.put(element, key);
        return key;
    }

    public Key getKey(VariableElement parameter) {
        Key key = keyCache.get(parameter);
        if (key != null) {
            return key;
        }
        key = Key.create(parameter.asType(), getQualifier(parameter));
        keyCache.put(parameter, key);
        return key;
    }

    private boolean hasQualifierAnnotation(AnnotationMirror mirror) {
        DeclaredType type = mirror.getAnnotationType();
        TypeElement element = TYPE_ELEMENT_VISITOR.visit(type.asElement());
        return tool.hasQualifierAnnotation(element);
    }

    TypeTool tool() {
        return tool;
    }

    @Override
    public void clearCache() {
        keyCache.clear();
    }
}
