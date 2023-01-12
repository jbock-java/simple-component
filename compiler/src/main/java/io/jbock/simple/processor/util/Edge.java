package io.jbock.simple.processor.util;

import io.jbock.simple.processor.binding.InjectBinding;

public record Edge(InjectBinding request, InjectBinding dependency) {

    public InjectBinding source() {
        return dependency;
    }

    public InjectBinding destination() {
        return request;
    }
}
