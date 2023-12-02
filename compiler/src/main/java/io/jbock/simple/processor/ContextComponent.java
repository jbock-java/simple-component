package io.jbock.simple.processor;

import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.InjectBindingFactory;
import io.jbock.simple.processor.binding.KeyFactory;
import io.jbock.simple.processor.graph.TopologicalSorter;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.writing.Generator;

@Component
public interface ContextComponent {

    @Component.Builder
    interface Builder {
        Builder componentElement(ComponentElement componentElement);

        Builder tool(TypeTool tool);

        Builder injectBindingFactory(InjectBindingFactory injectBindingFactory);

        Builder keyFactory(KeyFactory keyFactory);

        ContextComponent build();
    }

    static ContextComponent create(
            ComponentElement component,
            TypeTool tool,
            InjectBindingFactory injectBindingFactory,
            KeyFactory keyFactory) {
        return ContextComponent_Impl.builder()
                .componentElement(component)
                .tool(tool)
                .injectBindingFactory(injectBindingFactory)
                .keyFactory(keyFactory)
                .build();
    }

    Generator generator();

    TopologicalSorter topologicalSorter();

    final class Factory {
        private final TypeTool tool;
        private final InjectBindingFactory injectBindingFactory;
        private final KeyFactory keyFactory;

        @Inject
        public Factory(
                TypeTool tool,
                InjectBindingFactory injectBindingFactory,
                KeyFactory keyFactory) {
            this.tool = tool;
            this.injectBindingFactory = injectBindingFactory;
            this.keyFactory = keyFactory;
        }

        public ContextComponent create(ComponentElement component) {
            return ContextComponent_Impl.builder()
                    .componentElement(component)
                    .tool(tool)
                    .injectBindingFactory(injectBindingFactory)
                    .keyFactory(keyFactory)
                    .build();
        }
    }
}
