package io.jbock.simple.processor.util;

import io.jbock.auto.common.MoreElements;
import io.jbock.javapoet.ClassName;
import jakarta.inject.Qualifier;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Optional;

public class Qualifiers {

    private final SafeElements elements;

    public Qualifiers(SafeElements elements) {
        this.elements = elements;
    }

    public Optional<SimpleAnnotation> getQualifier(Element element) {
        List<SimpleAnnotation> qualifiers = element.getAnnotationMirrors().stream()
                .filter(Qualifiers::hasQualifierAnnotation)
                .map(mirror -> SimpleAnnotation.create(mirror, elements))
                .toList();
        if (qualifiers.isEmpty()) {
            return Optional.empty();
        }
        if (qualifiers.size() == 1) {
            return Optional.of(qualifiers.get(0));
        }
        throw new ValidationFailure("Found more than one qualifier annotation", element);
    }

    private static boolean hasQualifierAnnotation(AnnotationMirror mirror) {
        DeclaredType type = mirror.getAnnotationType();
        TypeElement element = Visitors.TYPE_ELEMENT_VISITOR.visit(type.asElement());
        return getAnnotationMirror(element, ClassName.get(Qualifier.class)).isPresent();
    }

    private static Optional<AnnotationMirror> getAnnotationMirror(
            Element element, ClassName annotationName) {
        String annotationClassName = annotationName.canonicalName();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            TypeElement annotationTypeElement =
                    MoreElements.asType(annotationMirror.getAnnotationType().asElement());
            if (annotationTypeElement.getQualifiedName().contentEquals(annotationClassName)) {
                return Optional.of(annotationMirror);
            }
        }
        return Optional.empty();
    }
}
