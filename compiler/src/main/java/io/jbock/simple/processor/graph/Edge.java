package io.jbock.simple.processor.graph;

import io.jbock.simple.processor.binding.Binding;

/**
 * Edge(FROM: source, TO: destination) :== source "IS INJECTED AT" destination 
 */
record Edge(Binding source, Binding destination) {

    @Override
    public String toString() {
        return "[" + source + "->" + destination + ']';
    }
}
