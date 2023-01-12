package io.jbock.simple.processor.util;

import io.jbock.simple.processor.binding.InjectBinding;

public record Edge(InjectBinding request, InjectBinding dependency) {
}
