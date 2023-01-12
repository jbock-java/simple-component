package io.jbock.simple.processor;

import io.jbock.simple.processor.step.ComponentStep;
import io.jbock.simple.processor.step.InjectStep;
import io.jbock.simple.processor.util.SafeElements;
import io.jbock.simple.processor.util.SafeTypes;
import io.jbock.simple.processor.util.SourceFileGenerator;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.Util;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

final class ProcessorComponent {

    private final Util util;
    private final TypeTool tool;
    private final Messager messager;
    private final SourceFileGenerator sourceFileGenerator;

    ProcessorComponent(ProcessingEnvironment processingEnvironment) {
        SafeTypes types = new SafeTypes(processingEnvironment.getTypeUtils());
        SafeElements elements = new SafeElements(processingEnvironment.getElementUtils());
        Filer filer = processingEnvironment.getFiler();
        this.tool = new TypeTool(elements, types);
        this.util = new Util(types, tool);
        this.messager = processingEnvironment.getMessager();
        this.sourceFileGenerator = new SourceFileGenerator(filer, messager);
    }

    ComponentStep componentStep() {
        return new ComponentStep();
    }

    InjectStep injectStep() {
        return new InjectStep();
    }
}
