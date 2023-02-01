package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.processor.util.TypeNames;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.validation.ExecutableElementValidator;
import io.jbock.simple.processor.validation.InjectBindingValidator;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class InjectStep implements Step {

    private final InjectBindingValidator validator;
    private final ExecutableElementValidator executableElementValidator;
    private final Messager messager;

    public InjectStep(
            InjectBindingValidator validator,
            ExecutableElementValidator executableElementValidator,
            Messager messager) {
        this.validator = validator;
        this.executableElementValidator = executableElementValidator;
        this.messager = messager;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(
                TypeNames.JAVAX_INJECT,
                TypeNames.JAKARTA_INJECT,
                TypeNames.SIMPLE_INJECT);
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        try {
            List<Element> elements = elementsByAnnotation.values().stream()
                    .flatMap(Set::stream)
                    .collect(Collectors.toList());
            List<ExecutableElement> constructors = constructorsIn(elements);
            List<ExecutableElement> methods = methodsIn(elements);
            for (ExecutableElement constructor : constructors) {
                executableElementValidator.validate(constructor);
                validator.validateConstructor(constructor);
            }
            for (ExecutableElement method : methods) {
                executableElementValidator.validate(method);
                validator.validateStaticMethod(method);
            }
        } catch (ValidationFailure f) {
            f.writeTo(messager);
        }
        return Set.of();
    }
}
