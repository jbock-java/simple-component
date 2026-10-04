package io.jbock.simple.processor.graph;

import io.jbock.simple.processor.binding.Node;

/**
 * Edge(FROM: source, TO: destination) :== source "IS INJECTED AT" destination
 */
record Edge(Node source, Node destination) {

    @Override
    public String toString() {
        return "[" + source + "->" + destination + ']';
    }
}
