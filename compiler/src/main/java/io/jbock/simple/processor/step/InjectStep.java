package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.processor.access.AccessGroup;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.util.InjectBindingRegistry;
import io.jbock.simple.processor.util.InjectBindingValidator;
import io.jbock.simple.processor.util.SpecWriter;
import io.jbock.simple.processor.util.ValidationFailure;
import jakarta.inject.Inject;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InjectStep implements Step {

    private final InjectBindingRegistry registry;
    private final InjectBindingValidator validator;
    private final Messager messager;
    private final SpecWriter specWriter;

    public InjectStep(
            InjectBindingRegistry registry,
            InjectBindingValidator validator,
            Messager messager,
            SpecWriter specWriter) {
        this.registry = registry;
        this.validator = validator;
        this.messager = messager;
        this.specWriter = specWriter;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(Inject.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        try {
            List<Element> elements = elementsByAnnotation.values().stream().flatMap(Set::stream).toList();
            List<ExecutableElement> constructors = ElementFilter.constructorsIn(elements);
            List<ExecutableElement> methods = ElementFilter.methodsIn(elements);
            for (ExecutableElement constructor : constructors) {
                InjectBinding b = registry.registerConstructor(constructor);
                validator.validateConstructor(constructor);
                AccessGroup accessGroup = AccessGroup.create(b);
                specWriter.write(accessGroup.generatedClass(), accessGroup.generate());
            }
            for (ExecutableElement method : methods) {
                InjectBinding b = registry.registerFactoryMethod(method);
                validator.validateStaticMethod(method);
                AccessGroup accessGroup = AccessGroup.create(b);
                specWriter.write(accessGroup.generatedClass(), accessGroup.generate());
            }
        } catch (ValidationFailure f) {
            f.writeTo(messager);
        }
        return Set.of();
    }
}
