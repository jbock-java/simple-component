package io.jbock.simple.processor.writing;

import io.jbock.simple.processor.binding.Binding;

final class NamedBinding {

    private final Binding binding;
    private final String name;
    private final String auxName;
    private final boolean componentRequest;

    NamedBinding(
            Binding binding,
            String name,
            String auxName,
            boolean componentRequest) {
        this.binding = binding;
        this.name = name;
        this.auxName = auxName;
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

    String auxName() {
        return auxName;
    }

    @Override
    public String toString() {
        return binding.toString();
    }
}
