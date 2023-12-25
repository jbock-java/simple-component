package io.jbock.simple.processor.writing;

import io.jbock.javapoet.ParameterSpec;
import io.jbock.simple.processor.binding.Key;

import java.util.Map;
import java.util.function.Function;

public final class Context {

    private final Map<Key, NamedBinding> sorted;
    private final Function<Key, ParameterSpec> names;

    public Context(Map<Key, NamedBinding> sorted, Function<Key, ParameterSpec> names) {
        this.sorted = sorted;
        this.names = names;
    }

    Map<Key, NamedBinding> sorted() {
        return sorted;
    }

    Function<Key, ParameterSpec> names() {
        return names;
    }
}
