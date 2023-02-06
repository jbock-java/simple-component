package io.jbock.simple.processor.util;

import javax.lang.model.type.TypeMirror;

public class ProviderType {

    private final ProviderKind kind;
    private final TypeMirror innerType;

    public ProviderType(ProviderKind kind, TypeMirror innerType) {
        this.kind = kind;
        this.innerType = innerType;
    }

    enum ProviderKind {
        SIMPLE(TypeNames.SIMPLE_PROVIDER),
        JAVAX(TypeNames.JAVAX_PROVIDER),
        JAKARTA(TypeNames.JAKARTA_PROVIDER);

        private final String className;

        ProviderKind(String className) {
            this.className = className;
        }
    }

    @Override
    public String toString() {
        return kind.className + "<" + innerType + ">";
    }
}
