package io.jbock.simple.processor.util;

import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.InjectBinding;

public final class DuplicateBinding {

    public static void check(Binding b, InjectBinding other) {
        if (other == null) {
            return;
        }
        throw new ValidationFailure("There is a conflicting binding: " + other.signature(), b.element());
    }

    private DuplicateBinding() {
    }
}
