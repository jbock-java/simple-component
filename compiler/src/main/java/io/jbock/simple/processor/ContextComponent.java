package io.jbock.simple.processor;

import io.jbock.simple.Component;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.InjectBindingFactory;
import io.jbock.simple.processor.binding.KeyFactory;
import io.jbock.simple.processor.graph.TopologicalSorter;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.writing.Generator;

@Component
public interface ContextComponent {

    @Component.Factory
    interface Factory {
        ContextComponent create(
                ComponentElement component,
                InjectBindingFactory injectBindingFactory,
                KeyFactory keyFactory);
    }

    static ContextComponent create(
            ComponentElement component,
            InjectBindingFactory injectBindingFactory,
            KeyFactory keyFactory) {
        return ContextComponent_Impl.factory().create(
                component,
                injectBindingFactory,
                keyFactory);
    }

    @Provides
    static TypeTool provideTypeTool(KeyFactory keyFactory) {
        return keyFactory.tool();
    }

    Generator generator();

    TopologicalSorter topologicalSorter();
}
