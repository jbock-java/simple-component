package io.jbock.simple.processor.writing;

import io.jbock.simple.processor.binding.Binding;

final class NamedBinding {

    private final Binding binding;
    private final String name;
    private final boolean componentRequest;

    NamedBinding(
            Binding binding,
            String name,
            boolean componentRequest) {
        this.binding = binding;
        this.name = name;
        this.componentRequest = componentRequest;
    }

    Binding binding() {
        return binding;
    }

    String name() {
        return name;
    }

    boolean isComponentRequest() {
        return componentRequest;
    }

    @Override
    public String toString() {
        return binding.toString();
    }
}
