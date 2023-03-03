package io.jbock.simple.processor.binding;

import io.jbock.javapoet.TypeName;
import io.jbock.simple.processor.util.SimpleAnnotation;

import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.Optional;

public final class Key {

    private final TypeMirror type;
    private final int hashCode;
    private final TypeName typeName;
    private final Optional<SimpleAnnotation> qualifier;

    private Key(
            TypeMirror type,
            TypeName typeName,
            Optional<SimpleAnnotation> qualifier) {
        this.type = type;
        this.typeName = typeName;
        this.qualifier = qualifier;
        this.hashCode = computeHashCode(typeName, qualifier);
    }

    static Key create(TypeMirror mirror, Optional<SimpleAnnotation> qualifier) {
        TypeName typeName = TypeName.get(mirror);
        return new Key(mirror, typeName, qualifier);
    }

    public Key changeType(TypeMirror newType) {
        return create(newType, qualifier);
    }

    @Override
    public String toString() {
        if (qualifier.isEmpty()) {
            return typeName.toString();
        }
        return typeName + " with qualifier @" + qualifier.orElseThrow();
    }

    public TypeName typeName() {
        return typeName;
    }

    public TypeMirror type() {
        return type;
    }

    public Optional<SimpleAnnotation> qualifier() {
        return qualifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return typeName.equals(key.typeName) && qualifier.equals(key.qualifier);
    }

    private static int computeHashCode(
            TypeName typeName,
            Optional<SimpleAnnotation> qualifier) {
        return Objects.hash(typeName, qualifier);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
