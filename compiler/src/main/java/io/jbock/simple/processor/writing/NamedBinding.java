package io.jbock.simple.processor.writing;

import io.jbock.simple.processor.binding.Binding;

public record NamedBinding(Binding binding, String name) {
}
