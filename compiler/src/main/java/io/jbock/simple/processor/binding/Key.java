package io.jbock.simple.processor.binding;

import io.jbock.javapoet.TypeName;

import java.util.Objects;
import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;

public final class Key {

    private final TypeName typeName;

    private final Supplier<String> suggestedVariableName = memoize(() -> {
        String[] tokens = typeName().toString().split("[.]");
        String simpleName = tokens[tokens.length - 1];
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    });


    public Key(TypeName typeName) {
        this.typeName = typeName;
    }

    public String suggestedVariableName() {
        return suggestedVariableName.get();
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
