package io.jbock.simple.processor;

import io.jbock.simple.Component;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.InjectBindingFactory;
import io.jbock.simple.processor.graph.GraphFactory;
import io.jbock.simple.processor.graph.TopologicalSorter;
import io.jbock.simple.processor.util.SafeElements;
import io.jbock.simple.processor.util.SafeTypes;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.writing.Generator;

@Component
public interface ContextComponent {

    @Component.Factory
    interface Factory {
        ContextComponent create(
                ComponentElement component,
                InjectBindingFactory injectBindingFactory,
                TypeTool tool);
    }

    static ContextComponent create(
            ComponentElement component,
            InjectBindingFactory injectBindingFactory,
            TypeTool tool) {
        return ContextComponent_Impl.factory().create(
                component,
                injectBindingFactory,
                tool);
    }

    @Provides
    static SafeElements provideElements(TypeTool tool) {
        return tool.elements();
    }

    @Provides
    static SafeTypes provideTypes(TypeTool tool) {
        return tool.types();
    }

    @Provides
    static GraphFactory provideGraphFactory(
            InjectBindingFactory injectBindingFactory,
            ComponentElement component) {
        return GraphFactory.create(component, injectBindingFactory);
    }

    Generator generator();

    TopologicalSorter topologicalSorter();
}
