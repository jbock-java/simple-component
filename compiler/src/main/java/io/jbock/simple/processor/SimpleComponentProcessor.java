package io.jbock.simple.processor;

import io.jbock.auto.common.BasicAnnotationProcessor;
import io.jbock.simple.processor.util.ClearableCache;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import java.util.List;

public final class SimpleComponentProcessor extends BasicAnnotationProcessor {

    private List<ClearableCache> clearableCaches = List.of();

    @Override
    protected Iterable<? extends Step> steps() {
        ProcessorComponent component = ProcessorComponent_Impl.factory().create(processingEnv);
        clearableCaches = component.clearableCaches();
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

    @Override
    protected void postRound(RoundEnvironment roundEnv) {
        for (ClearableCache cache : clearableCaches) {
            cache.clearCache();
        }
    }
}
