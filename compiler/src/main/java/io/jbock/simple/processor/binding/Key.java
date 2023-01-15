package io.jbock.simple.processor.binding;

import io.jbock.javapoet.TypeName;
import io.jbock.simple.processor.util.SimpleAnnotation;

import java.util.Objects;
import java.util.Optional;

public final class Key {

    private final TypeName typeName;
    private final Optional<SimpleAnnotation> qualifier;

    public Key(TypeName typeName,
               Optional<SimpleAnnotation> qualifier) {
        this.typeName = typeName;
        this.qualifier = qualifier;
    }

    @Override
    public String toString() {
        if (qualifier.isEmpty()) {
            return "" + typeName;
        }
        return "" + typeName + "-" + qualifier.orElseThrow();
    }

    public TypeName typeName() {
        return typeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return typeName.equals(key.typeName) && qualifier.equals(key.qualifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName, qualifier);
    }
}
