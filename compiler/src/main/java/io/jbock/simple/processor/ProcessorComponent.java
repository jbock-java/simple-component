package io.jbock.simple.processor;

import io.jbock.simple.processor.graph.GraphFactory;
import io.jbock.simple.processor.graph.TopologicalSorter;
import io.jbock.simple.processor.step.ComponentFactoryStep;
import io.jbock.simple.processor.step.ComponentStep;
import io.jbock.simple.processor.step.InjectStep;
import io.jbock.simple.processor.step.ProvidesStep;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.validation.ExecutableElementValidator;
import io.jbock.simple.processor.validation.InjectBindingValidator;
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
    private final ComponentImpl componentImpl;
    private final Function<ComponentElement, Generator> generatorFactory;
    private final Function<ComponentElement, GraphFactory> bindingRegistryFactory;
    private final Qualifiers qualifiers;
    private final SpecWriter specWriter;
    private final ComponentStep componentStep;
    private final InjectStep injectStep;
    private final ProvidesStep providesStep;
    private final InjectBindingValidator injectBindingValidator;
    private final TypeElementValidator typeElementValidator;
    private final ExecutableElementValidator executableElementValidator;
    private final SafeTypes types;
    private final SafeElements elements;
    private final ComponentFactoryStep componentFactoryStep;
    private final TopologicalSorter topologicalSorter;

    ProcessorComponent(ProcessingEnvironment processingEnvironment) {
        this.types = new SafeTypes(processingEnvironment.getTypeUtils());
        this.elements = new SafeElements(processingEnvironment.getElementUtils());
        Filer filer = processingEnvironment.getFiler();
        this.tool = new TypeTool(elements, types);
        this.qualifiers = new Qualifiers(tool);
        this.messager = processingEnvironment.getMessager();
        this.sourceFileGenerator = new SourceFileGenerator(filer, messager);
        this.injectBindingValidator = new InjectBindingValidator(qualifiers);
        this.typeElementValidator = new TypeElementValidator();
        this.executableElementValidator = new ExecutableElementValidator(tool, typeElementValidator);
        this.componentImpl = new ComponentImpl();
        this.bindingRegistryFactory = GraphFactory::create;
        this.generatorFactory = component -> new Generator(componentImpl, component);
        this.specWriter = new SpecWriter(sourceFileGenerator, messager);
        this.topologicalSorter = new TopologicalSorter(bindingRegistryFactory);
        this.componentStep = new ComponentStep(messager, tool, qualifiers, typeElementValidator, generatorFactory, topologicalSorter, specWriter);
        this.injectStep = new InjectStep(injectBindingValidator, executableElementValidator, messager);
        this.providesStep = new ProvidesStep(messager);
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
