package io.jbock.simple.processor;

import io.jbock.simple.processor.step.ComponentStep;
import io.jbock.simple.processor.step.InjectStep;
import io.jbock.simple.processor.util.ComponentRegistry;
import io.jbock.simple.processor.util.InjectBindingRegistry;
import io.jbock.simple.processor.util.SafeElements;
import io.jbock.simple.processor.util.SafeTypes;
import io.jbock.simple.processor.util.SourceFileGenerator;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.Util;
import io.jbock.simple.processor.writing.ComponentImpl;
import io.jbock.simple.processor.writing.Generator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

final class ProcessorComponent {

    private final Util util;
    private final TypeTool tool;
    private final Messager messager;
    private final SourceFileGenerator sourceFileGenerator;
    private final InjectBindingRegistry injectBindingRegistry;
    private final ComponentRegistry componentRegistry;
    private final ComponentImpl componentImpl;
    private final Generator.Factory generatorFactory;
    private final ComponentStep componentStep;
    private final InjectStep injectStep;
    private final SafeTypes types;
    private final SafeElements elements;

    ProcessorComponent(ProcessingEnvironment processingEnvironment) {
        this.types = new SafeTypes(processingEnvironment.getTypeUtils());
        this.elements = new SafeElements(processingEnvironment.getElementUtils());
        Filer filer = processingEnvironment.getFiler();
        this.componentRegistry = new ComponentRegistry();
        this.tool = new TypeTool(elements, types);
        this.util = new Util(types, tool);
        this.messager = processingEnvironment.getMessager();
        this.sourceFileGenerator = new SourceFileGenerator(filer, messager);
        this.injectBindingRegistry = new InjectBindingRegistry();
        this.componentImpl = new ComponentImpl();
        this.generatorFactory = component -> new Generator(injectBindingRegistry.createBindingRegistry(component), componentImpl, component);
        this.componentStep = new ComponentStep(componentRegistry, messager, tool);
        this.injectStep = new InjectStep(injectBindingRegistry);
    }

    ComponentStep componentStep() {
        return componentStep;
    }

    InjectStep injectStep() {
        return new InjectStep(injectBindingRegistry);
    }

    ComponentRegistry componentRegistry() {
        return componentRegistry;
    }

    SourceFileGenerator sourceFileGenerator() {
        return sourceFileGenerator;
    }

    Generator.Factory generatorFactory() {
        return generatorFactory;
    }

    Messager messager() {
        return messager;
    }

    SafeTypes types() {
        return types;
    }

    SafeElements elements() {
        return elements;
    }

    TypeTool tool() {
        return tool;
    }
}
