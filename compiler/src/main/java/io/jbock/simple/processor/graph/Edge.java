package io.jbock.simple.processor.graph;

import io.jbock.simple.processor.binding.Binding;

import java.util.Objects;

/**
 * Edge(FROM: source, TO: destination) :== source "IS INJECTED AT" destination 
 */
final class Edge {

    private final Binding source;
    private final Binding destination;

    Edge(Binding source, Binding destination) {
        this.source = source;
        this.destination = destination;
    }

    Binding source() {
        return source;
    }

    Binding destination() {
        return destination;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Edge) obj;
        return Objects.equals(this.source, that.source) &&
                Objects.equals(this.destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }

    @Override
    public String toString() {
        return "[" + source + "->" + destination + ']';
    }
}
