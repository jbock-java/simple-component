package io.jbock.simple.processor.util;

import io.jbock.simple.processor.binding.Binding;

/**
 * Edge(source, destination) :== source "IS INJECTED AT" destination 
 */
public record Edge(
        Binding source,
        Binding destination) {
}
