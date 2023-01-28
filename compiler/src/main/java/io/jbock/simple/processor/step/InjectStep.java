package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.ExecutableElementValidator;
import io.jbock.simple.processor.util.InjectBindingValidator;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        return Set.of(Inject.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        try {
            List<Element> elements = elementsByAnnotation.values().stream()
                    .flatMap(Set::stream)
                    .collect(Collectors.toList());
            List<ExecutableElement> constructors = ElementFilter.constructorsIn(elements);
            List<ExecutableElement> methods = ElementFilter.methodsIn(elements);
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
