package io.jbock.simple.processor.writing;

import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.ProviderBinding;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;

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

    FieldSpec field() {
        TypeName fieldType;
        if (binding instanceof ProviderBinding) {
            fieldType = ((ProviderBinding) binding).providerType();
        } else {
            fieldType = binding.key().typeName();
        }
        return FieldSpec.builder(fieldType, name, PRIVATE, FINAL).build();
    }
}
