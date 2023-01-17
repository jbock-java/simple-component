package io.jbock.simple.processor;

import io.jbock.auto.common.BasicAnnotationProcessor;

import javax.lang.model.SourceVersion;
import java.util.List;

public final class SimpleComponentProcessor extends BasicAnnotationProcessor {

    @Override
    protected Iterable<? extends Step> steps() {
        ProcessorComponent component = new ProcessorComponent(processingEnv);
        return List.of(
                component.injectStep(),
                component.componentStep());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
