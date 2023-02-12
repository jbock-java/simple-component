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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KeyFactory {

    private final TypeTool tool;

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

    public TypeTool tool() {
        return tool;
    }
}
