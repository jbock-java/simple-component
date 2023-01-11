package io.jbock.simple.processor;

import io.jbock.auto.common.BasicAnnotationProcessor;

import javax.lang.model.SourceVersion;
import java.util.List;

public final class SimpleComponentProcessor extends BasicAnnotationProcessor {

    @Override
    protected Iterable<? extends Step> steps() {
        return List.of();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
