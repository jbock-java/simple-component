package io.jbock.simple.processor.graph;

public interface AbstractEdge<N> {
    N source();
    N destination();
}
