package io.jbock.simple.processor;

import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.KeyFactory;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.writing.ComponentImpl;
import io.jbock.simple.processor.writing.ContextModule;

import javax.lang.model.element.TypeElement;

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
