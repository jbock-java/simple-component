package io.jbock.simple.processor;

import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.KeyFactory;
import io.jbock.simple.processor.graph.TopologicalSorter;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.writing.ComponentImpl;
import io.jbock.simple.processor.writing.Context;
import io.jbock.simple.processor.writing.ContextModule;
import io.jbock.simple.processor.writing.NamedBinding;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Map;

@Component(modules = ContextModule.class)
public interface ContextComponent {

    @Component.Builder
    interface Builder {
        Builder typeElement(TypeElement typeElement);

        Builder tool(TypeTool tool);

        ContextComponent build();
    }

    KeyFactory keyFactory();

    ComponentElement componentElement();

    ComponentImpl componentImpl();

    @Provides
    static Context createContext(
            TopologicalSorter topologicalSorter,
            KeyFactory keyFactory) {
        List<Binding> bindings = topologicalSorter.sortedBindings();
        Map<Key, NamedBinding> sorted = ContextModule.addNames(keyFactory, bindings);
        return new Context(sorted, ContextModule.createNames(sorted));
    }

    final class Factory {
        private final TypeTool tool;

        @Inject
        public Factory(
                TypeTool tool) {
            this.tool = tool;
        }

        public ContextComponent create(TypeElement typeElement) {
            return ContextComponent_Impl.builder()
                    .typeElement(typeElement)
                    .tool(tool)
                    .build();
        }
    }
}
