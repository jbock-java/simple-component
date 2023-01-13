package io.jbock.simple.processor.binding;

import io.jbock.javapoet.TypeName;

public record Key(TypeName typeName) {

    @Override
    public String toString() {
        return "" + typeName;
    }
}
