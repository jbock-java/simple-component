package io.jbock.simple.processor.util;

import javax.lang.model.type.TypeMirror;

public record ProviderType(
        ProviderType.ProviderKind kind,
        TypeMirror innerType) {

    public enum ProviderKind {
        JAVAX(TypeNames.JAVAX_PROVIDER),
        JAKARTA(TypeNames.JAKARTA_PROVIDER),
        SUPPLIER(TypeNames.SUPPLIER)
        ;

        private final String className;

        ProviderKind(String className) {
            this.className = className;
        }

        public String className() {
            return className;
        }
    }

    @Override
    public String toString() {
        return kind.className + "<" + innerType + ">";
    }
}
