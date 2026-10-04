package io.jbock.simple.processor.writing;

import io.jbock.simple.processor.binding.Key;

import java.util.Map;

public final class Context {

    private final Map<Key, NamedBinding> sorted;

    public Context(Map<Key, NamedBinding> sorted) {
        this.sorted = sorted;
    }

    Map<Key, NamedBinding> sorted() {
        return sorted;
    }
}
