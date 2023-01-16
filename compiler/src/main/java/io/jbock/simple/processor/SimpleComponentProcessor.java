package io.jbock.simple.processor;

import io.jbock.auto.common.BasicAnnotationProcessor;
import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.JavaFile;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.access.AccessGroup;
import io.jbock.simple.processor.access.AccessGroups;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import java.util.List;

public final class SimpleComponentProcessor extends BasicAnnotationProcessor {

    private ProcessorComponent component;

    @Override
    protected Iterable<? extends Step> steps() {
        component = new ProcessorComponent(processingEnv);
        return List.of(
                component.injectStep(),
                component.componentStep());
    }

    @Override
    protected void postRound(RoundEnvironment roundEnv) {
        boolean lastRound = roundEnv.getRootElements().isEmpty();
        if (lastRound && roundEnv.processingOver()) {
            for (ComponentElement componentElement : component.componentRegistry().components()) {
                try {
                    TypeSpec typeSpec = component.generatorFactory().create(componentElement).generate();
                    writeSpec(componentElement.generatedClass(), typeSpec);
                } catch (ValidationFailure f) {
                    f.writeTo(component.messager());
                }
            }
            for (AccessGroup group : AccessGroups.create(component.injectRegistry().allBindings()).groups()) {
                writeSpec(group.generatedClass(), group.generate());
            }
        }
    }

    private void writeSpec(ClassName generatedClass, TypeSpec typeSpec) {
        if (typeSpec.originatingElements.size() != 1) {
            throw new AssertionError();
        }
        String packageName = generatedClass.packageName();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .skipJavaLangImports(true)
                .build();
        component.sourceFileGenerator().write(typeSpec.originatingElements.get(0), javaFile);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
