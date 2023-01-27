package io.jbock.simple.processor.util;

import javax.lang.model.element.TypeElement;

public class ModuleElement {

    private final TypeElement element;
    private final TypeTool tool;

    private ModuleElement(TypeElement element, TypeTool tool) {
        this.element = element;
        this.tool = tool;
    }

    static ModuleElement create(TypeElement element, TypeTool tool) {
        return new ModuleElement(element, tool);
    }
}
