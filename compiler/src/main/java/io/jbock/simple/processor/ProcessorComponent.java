package io.jbock.simple.processor;

import io.jbock.simple.processor.step.ComponentFactoryStep;
import io.jbock.simple.processor.step.ComponentStep;
import io.jbock.simple.processor.step.InjectStep;
import io.jbock.simple.processor.util.BindingRegistry;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.ComponentRegistry;
import io.jbock.simple.processor.util.ExecutableElementValidator;
import io.jbock.simple.processor.util.InjectBindingValidator;
import io.jbock.simple.processor.util.Qualifiers;
import io.jbock.simple.processor.util.SafeElements;
import io.jbock.simple.processor.util.SafeTypes;
import io.jbock.simple.processor.util.SourceFileGenerator;
import io.jbock.simple.processor.util.SpecWriter;
import io.jbock.simple.processor.util.TypeElementValidator;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.writing.ComponentImpl;
import io.jbock.simple.processor.writing.Generator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import java.util.function.Function;

final class ProcessorComponent {

    private final TypeTool tool;
    private final Messager messager;
    private final SourceFileGenerator sourceFileGenerator;
    private final ComponentRegistry componentRegistry;
    private final ComponentImpl componentImpl;
    private final Function<ComponentElement, Generator> generatorFactory;
    private final Function<ComponentElement, BindingRegistry> bindingRegistryFactory;
    private final Qualifiers qualifiers;
    private final SpecWriter specWriter;
    private final ComponentStep componentStep;
    private final InjectStep injectStep;
    private final InjectBindingValidator injectBindingValidator;
    private final TypeElementValidator typeElementValidator;
    private final ExecutableElementValidator executableElementValidator;
    private final SafeTypes types;
    private final SafeElements elements;
    private final ComponentFactoryStep componentFactoryStep;

    ProcessorComponent(ProcessingEnvironment processingEnvironment) {
        this.types = new SafeTypes(processingEnvironment.getTypeUtils());
        this.elements = new SafeElements(processingEnvironment.getElementUtils());
        qualifiers = new Qualifiers(elements);
        Filer filer = processingEnvironment.getFiler();
        this.componentRegistry = new ComponentRegistry();
        this.tool = new TypeTool(elements, types);
        this.messager = processingEnvironment.getMessager();
        this.sourceFileGenerator = new SourceFileGenerator(filer, messager);
        this.injectBindingValidator = new InjectBindingValidator(tool);
        this.typeElementValidator = new TypeElementValidator();
        this.executableElementValidator = new ExecutableElementValidator(tool, typeElementValidator);
        this.componentImpl = new ComponentImpl();
        this.bindingRegistryFactory = BindingRegistry.factory();
        this.generatorFactory = component -> Generator.create(bindingRegistryFactory.apply(component), componentImpl, component);
        this.specWriter = new SpecWriter(sourceFileGenerator, messager);
        this.componentStep = new ComponentStep(componentRegistry, messager, tool, qualifiers, typeElementValidator, generatorFactory, specWriter);
        this.injectStep = new InjectStep(injectBindingValidator, executableElementValidator, messager);
        this.componentFactoryStep = new ComponentFactoryStep(messager, typeElementValidator);
    }

    ComponentStep componentStep() {
        return componentStep;
    }

    InjectStep injectStep() {
        return injectStep;
    }

    ComponentFactoryStep componentFactoryStep() {
        return componentFactoryStep;
    }
}
