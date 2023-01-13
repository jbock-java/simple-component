package io.jbock.simple.processor.binding;

import java.util.Objects;

public abstract sealed class Binding permits InjectBinding {

    private final Key key;

    Binding(Key key) {
        this.key = key;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Binding binding = (Binding) o;
        return key.equals(binding.key);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(key);
    }

    public final Key key() {
        return key;
    }
}
