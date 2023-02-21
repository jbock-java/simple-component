package io.jbock.simple.processor;

import io.jbock.auto.common.BasicAnnotationProcessor;

import javax.lang.model.SourceVersion;
import java.util.List;

public final class SimpleComponentProcessor extends BasicAnnotationProcessor {

    @Override
    protected Iterable<? extends Step> steps() {
        ProcessorComponent component = ProcessorComponent_Impl.factory().create(processingEnv);
        return List.of(
                component.injectStep(),
                component.providesStep(),
                component.componentStep(),
                component.componentFactoryStep());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
