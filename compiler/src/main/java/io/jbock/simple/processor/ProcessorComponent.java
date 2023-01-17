package io.jbock.simple.processor;

import io.jbock.simple.processor.step.ComponentStep;
import io.jbock.simple.processor.step.InjectStep;
import io.jbock.simple.processor.util.ComponentElementValidator;
import io.jbock.simple.processor.util.ComponentRegistry;
import io.jbock.simple.processor.util.InjectBindingRegistry;
import io.jbock.simple.processor.util.InjectBindingValidator;
import io.jbock.simple.processor.util.Qualifiers;
import io.jbock.simple.processor.util.SafeElements;
import io.jbock.simple.processor.util.SafeTypes;
import io.jbock.simple.processor.util.SourceFileGenerator;
import io.jbock.simple.processor.util.SpecWriter;
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
    private final Qualifiers qualifiers;
    private final SpecWriter specWriter;
    private final ComponentStep componentStep;
    private final InjectStep injectStep;
    private final InjectBindingValidator injectBindingValidator;
    private final ComponentElementValidator componentElementValidator;
    private final SafeTypes types;
    private final SafeElements elements;

    ProcessorComponent(ProcessingEnvironment processingEnvironment) {
        this.types = new SafeTypes(processingEnvironment.getTypeUtils());
        this.elements = new SafeElements(processingEnvironment.getElementUtils());
        qualifiers = new Qualifiers(elements);
        Filer filer = processingEnvironment.getFiler();
        this.componentRegistry = new ComponentRegistry();
        this.tool = new TypeTool(elements, types);
        this.util = new Util(types, tool);
        this.messager = processingEnvironment.getMessager();
        this.sourceFileGenerator = new SourceFileGenerator(filer, messager);
        this.injectBindingValidator = new InjectBindingValidator(tool);
        this.injectBindingRegistry = new InjectBindingRegistry(qualifiers, tool);
        this.componentImpl = new ComponentImpl();
        this.componentElementValidator = new ComponentElementValidator();
        this.generatorFactory = component -> new Generator(injectBindingRegistry.createBindingRegistry(component), componentImpl, component);
        this.specWriter = new SpecWriter(sourceFileGenerator, messager);
        this.componentStep = new ComponentStep(componentRegistry, messager, tool, qualifiers, componentElementValidator, generatorFactory, specWriter);
        this.injectStep = new InjectStep(injectBindingRegistry, injectBindingValidator, messager, specWriter);
    }

    ComponentStep componentStep() {
        return componentStep;
    }

    InjectStep injectStep() {
        return injectStep;
    }

    ComponentRegistry componentRegistry() {
        return componentRegistry;
    }

    InjectBindingRegistry injectRegistry() {
        return injectBindingRegistry;
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
