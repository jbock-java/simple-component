package io.jbock.simple.processor.util;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.ProviderType.ProviderKind;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static io.jbock.simple.processor.util.TypeNames.JAKARTA_INJECT;
import static io.jbock.simple.processor.util.TypeNames.JAVAX_INJECT;
import static io.jbock.simple.processor.util.TypeNames.SIMPLE_INJECT;
import static io.jbock.simple.processor.util.Visitors.DECLARED_TYPE_VISITOR;

public record TypeTool(
        SafeElements elements,
        SafeTypes types) {

    @Inject
    public TypeTool {
    }

    /**
     * Works for classes with no type parameters.
     */
    public boolean isSameType(TypeMirror mirror, String canonicalName) {
        TypeElement typeElement = elements.getTypeElement(canonicalName);
        if (typeElement == null) {
            return false;
        }
        return types.isSameType(mirror, typeElement.asType());
    }

    public boolean isSameType(TypeMirror mirror, TypeMirror other) {
        return types.isSameType(mirror, other);
    }

    public boolean hasInjectAnnotation(Element m) {
        if (m.getKind() == ElementKind.CLASS || m.getKind() == ElementKind.RECORD) {
            return m.getAnnotationMirrors().stream().anyMatch(mirror -> {
                DeclaredType annotationType = mirror.getAnnotationType();
                return isSameType(annotationType, SIMPLE_INJECT);
            });
        }
        if (m.getKind() != ElementKind.CONSTRUCTOR && m.getKind() != ElementKind.METHOD) {
            return false;
        }
        List<? extends AnnotationMirror> mirrors = m.getAnnotationMirrors();
        if (mirrors.isEmpty()) {
            return false;
        }
        return mirrors.stream().anyMatch(mirror -> {
            DeclaredType annotationType = mirror.getAnnotationType();
            return isSameType(annotationType, JAVAX_INJECT)
                    || isSameType(annotationType, JAKARTA_INJECT)
                    || isSameType(annotationType, SIMPLE_INJECT);
        });
    }

    public boolean hasQualifierAnnotation(Element m) {
        List<? extends AnnotationMirror> mirrors = m.getAnnotationMirrors();
        if (mirrors.isEmpty()) {
            return false;
        }
        return mirrors.stream().anyMatch(mirror -> {
            DeclaredType annotationType = mirror.getAnnotationType();
            return isSameType(annotationType, TypeNames.SIMPLE_QUALIFIER)
                    || isSameType(annotationType, TypeNames.JAKARTA_QUALIFIER)
                    || isSameType(annotationType, TypeNames.JAVAX_QUALIFIER);
        });
    }

    public Optional<ProviderType> getProviderType(TypeMirror mirror) {
        DeclaredType declaredType = DECLARED_TYPE_VISITOR.visit(mirror);
        if (declaredType == null) {
            return Optional.empty();
        }
        for (ProviderKind providerKind : ProviderKind.values()) {
            TypeMirror m = getSingleTypeArgument(declaredType, elements.getTypeElement(providerKind.className()));
            if (m != null) {
                return Optional.of(new ProviderType(providerKind, m));
            }
        }
        return Optional.empty();
    }

    private TypeMirror getSingleTypeArgument(
            DeclaredType declaredType, TypeElement someClass) {
        if (someClass == null) {
            return null;
        }
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() != 1) {
            return null;
        }
        if (types.isSameType(types.erasure(declaredType), types.erasure(someClass.asType()))) {
            return typeArguments.getFirst();
        }
        return null;
    }
}
