package io.jbock.simple.processor.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Qualifiers {

    private final TypeTool tool;

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
}
