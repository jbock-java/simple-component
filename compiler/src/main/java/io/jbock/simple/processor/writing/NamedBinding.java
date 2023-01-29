package io.jbock.simple.processor.writing;

import io.jbock.simple.processor.binding.Binding;

public final class NamedBinding {

    private final Binding binding;
    private final String name;
    private final boolean componentRequest;

    public NamedBinding(
            Binding binding,
            String name,
            boolean componentRequest) {
        this.binding = binding;
        this.name = name;
        this.componentRequest = componentRequest;
    }

    public Binding binding() {
        return binding;
    }

    public String name() {
        return name;
    }

    public boolean isComponentRequest() {
        return componentRequest;
    }

    @Override
    public String toString() {
        return binding.toString();
    }
}
