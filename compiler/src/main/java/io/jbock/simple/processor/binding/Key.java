package io.jbock.simple.processor.binding;

import io.jbock.javapoet.TypeName;

import java.util.Objects;

public final class Key {

    private final TypeName typeName;

    public Key(TypeName typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "" + typeName;
    }

    public TypeName typeName() {
        return typeName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Key) obj;
        return Objects.equals(this.typeName, that.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName);
    }
}
