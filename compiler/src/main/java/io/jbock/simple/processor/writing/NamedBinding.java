package io.jbock.simple.processor.writing;

import io.jbock.simple.processor.binding.Binding;

public final class NamedBinding {

    private final Binding binding;
    private final String name;

    public NamedBinding(Binding binding, String name) {
        this.binding = binding;
        this.name = name;
    }

    public Binding binding() {
        return binding;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return binding.toString();
    }
}
