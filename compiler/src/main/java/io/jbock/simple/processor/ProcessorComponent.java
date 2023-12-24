package io.jbock.simple.processor;

import io.jbock.simple.Component;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.binding.InjectBindingCache;
import io.jbock.simple.processor.binding.KeyCache;
import io.jbock.simple.processor.step.ComponentFactoryStep;
import io.jbock.simple.processor.step.ComponentStep;
import io.jbock.simple.processor.step.InjectStep;
import io.jbock.simple.processor.step.ProvidesStep;
import io.jbock.simple.processor.util.ClearableCache;
import io.jbock.simple.processor.util.SafeElements;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;

@Component
interface ProcessorComponent {

    @Component.Factory
    interface Factory {
        ProcessorComponent create(ProcessingEnvironment processingEnvironment);
    }

    @Provides
    static Filer provideFiler(ProcessingEnvironment processingEnvironment) {
        return processingEnvironment.getFiler();
    }

    @Provides
    static Messager provideMessager(ProcessingEnvironment processingEnvironment) {
        return processingEnvironment.getMessager();
    }

    @Provides
    static Elements provideElements(ProcessingEnvironment processingEnvironment) {
        return processingEnvironment.getElementUtils();
    }

    @Provides
    static Types provideTypes(ProcessingEnvironment processingEnvironment) {
        return processingEnvironment.getTypeUtils();
    }

    @Provides
    static List<ClearableCache> caches(
            InjectBindingCache injectBindingCache,
            KeyCache keyCache,
            SafeElements safeElements) {
        return List.of(injectBindingCache, keyCache, safeElements);
    }

    ComponentStep componentStep();

    InjectStep injectStep();

    ProvidesStep providesStep();

    ComponentFactoryStep componentFactoryStep();

    List<ClearableCache> clearableCaches();
}
