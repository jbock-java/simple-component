package io.jbock.simple.processor.util;

import io.jbock.simple.Inject;

import javax.lang.model.element.Element;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;

/**
 * A wrapper around {@link Types} where none of the methods can return {@code null}.
 */
public final class SafeTypes {

    private final Types types;

    @Inject
    public SafeTypes(Types types) {
        this.types = types;
    }

    public Optional<Element> asElement(TypeMirror t) {
        return Optional.ofNullable(types.asElement(t));
    }

    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return types.isSameType(t1, t2);
    }

    public TypeMirror erasure(TypeMirror t) {
        return types.erasure(t);
    }
}
