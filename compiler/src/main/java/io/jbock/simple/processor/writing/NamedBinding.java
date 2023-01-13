package io.jbock.simple.processor.writing;

import io.jbock.simple.processor.binding.InjectBinding;

public record NamedBinding(InjectBinding binding, String name) {
}
