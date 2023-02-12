package io.jbock.simple.processor;

import io.jbock.simple.Component;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.InjectBindingFactory;
import io.jbock.simple.processor.graph.GraphFactory;
import io.jbock.simple.processor.step.ComponentFactoryStep;
import io.jbock.simple.processor.step.ComponentStep;
import io.jbock.simple.processor.step.InjectStep;
import io.jbock.simple.processor.writing.ComponentImpl;
import io.jbock.simple.processor.writing.Generator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.function.Function;

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
    static Function<ComponentElement, GraphFactory> provideBindingRegistryFactory(
            InjectBindingFactory injectBindingFactory) {
        return component -> GraphFactory.create(component, injectBindingFactory);
    }

    @Provides
    static Function<ComponentElement, Generator> provideGeneratorFactory(ComponentImpl componentImpl) {
        return component -> new Generator(componentImpl, component);
    }

    ComponentStep componentStep();

    InjectStep injectStep();

    ComponentFactoryStep componentFactoryStep();
}
