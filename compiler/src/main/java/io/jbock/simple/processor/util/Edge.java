package io.jbock.simple.processor.util;

import io.jbock.simple.processor.binding.Binding;

// An Injection: The source binding gets injected into destination binding.
public record Edge(
        Binding source,
        Binding destination) {
}
