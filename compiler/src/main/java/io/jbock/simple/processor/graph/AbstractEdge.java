package io.jbock.simple.processor.graph;

public interface AbstractEdge<E> {
    E source();
    E destination();
}
