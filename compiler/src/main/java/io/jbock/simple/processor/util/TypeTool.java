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

public final class TypeTool {

    private final SafeElements elements;
    private final SafeTypes types;

    @Inject
    public TypeTool(SafeElements elements, SafeTypes types) {
        this.elements = elements;
        this.types = types;
    }

    /**
     * Works for classes with no type parameters.
     */
    public boolean isSameType(TypeMirror mirror, Class<?> cl) {
        return isSameType(mirror, cl.getCanonicalName());
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

    public boolean hasInjectAnnotation(Element m) {
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
        return getSingleTypeArgument(mirror, elements.getTypeElement(TypeNames.JAVAX_PROVIDER))
                .map(m -> new ProviderType(ProviderKind.JAVAX, m))
                .or(() -> getSingleTypeArgument(mirror, elements.getTypeElement(TypeNames.JAKARTA_PROVIDER))
                        .map(m -> new ProviderType(ProviderKind.JAKARTA, m)))
                .or(() -> getSingleTypeArgument(mirror, elements.getTypeElement(TypeNames.SIMPLE_PROVIDER))
                        .map(m -> new ProviderType(ProviderKind.SIMPLE, m)));
    }

    private Optional<TypeMirror> getSingleTypeArgument(
            TypeMirror mirror, TypeElement someClass) {
        if (someClass == null) {
            return Optional.empty();
        }
        DeclaredType declaredType = DECLARED_TYPE_VISITOR.visit(mirror);
        if (declaredType == null) {
            return Optional.empty();
        }
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() != 1) {
            return Optional.empty();
        }
        if (types.isSameType(types.erasure(declaredType), types.erasure(someClass.asType()))) {
            return Optional.of(typeArguments.get(0));
        }
        return Optional.empty();
    }

    public SafeElements elements() {
        return elements;
    }

    public SafeTypes types() {
        return types;
    }
}
