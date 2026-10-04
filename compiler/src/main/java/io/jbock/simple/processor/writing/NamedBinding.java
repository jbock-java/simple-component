package io.jbock.simple.processor.writing;

import com.palantir.javapoet.ParameterSpec;
import io.jbock.simple.processor.binding.Node;

import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;

public final class NamedBinding {

    private final Node binding;
    private final String name;
    private final String auxName;
    private final boolean componentRequest;

    private Supplier<ParameterSpec> parameter = memoize(() -> {
        String name = name();
        return ParameterSpec.builder(binding().key().typeName(), name).build();
    });

    public NamedBinding(
            Node binding,
            String name,
            String auxName,
            boolean componentRequest) {
        this.binding = binding;
        this.name = name;
        this.auxName = auxName;
        this.componentRequest = componentRequest;
    }

    public Node binding() {
        return binding;
    }

    public String name() {
        return name;
    }

    boolean isComponentRequest() {
        return componentRequest;
    }

    String auxName() {
        return auxName;
    }

    public ParameterSpec parameter() {
        return parameter.get();
    }

    @Override
    public String toString() {
        return binding.toString();
    }
}
