package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.TypeNames;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.validation.ExecutableElementValidator;
import io.jbock.simple.processor.validation.InjectBindingValidator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class InjectStep implements Step {

    private final InjectBindingValidator validator;
    private final ExecutableElementValidator executableElementValidator;
    private final Messager messager;
    private final BindingRegistry bindingRegistry;

    @Inject
    public InjectStep(
            InjectBindingValidator validator,
            ExecutableElementValidator executableElementValidator,
            Messager messager,
            BindingRegistry bindingRegistry) {
        this.validator = validator;
        this.executableElementValidator = executableElementValidator;
        this.messager = messager;
        this.bindingRegistry = bindingRegistry;
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
                    .toList();
            for (ExecutableElement constructor : constructorsIn(elements)) {
                executableElementValidator.validate(constructor);
                validator.validateConstructor(constructor);
                bindingRegistry.register(constructor);
            }
            for (ExecutableElement method : methodsIn(elements)) {
                executableElementValidator.validate(method);
                validator.validateStaticMethod(method);
                bindingRegistry.register(method);
            }
            for (TypeElement tel : ElementFilter.typesIn(elements)) {
                List<ExecutableElement> constructors = constructorsIn(tel.getEnclosedElements());
                if (constructors.isEmpty()) {
                    throw new ValidationFailure("constructor not found", tel);
                }
                if (constructors.size() >= 2) {
                    throw new ValidationFailure("more than one constructor found", tel);
                }
                for (ExecutableElement constructor : constructors) {
                    executableElementValidator.validate(constructor);
                    validator.validateConstructor(constructor);
                    bindingRegistry.register(constructor);
                }
            }
            checkFields(elements);
        } catch (ValidationFailure f) {
            f.writeTo(messager);
        }
        return Set.of();
    }

    private void checkFields(List<Element> elements) {
        List<VariableElement> fields = fieldsIn(elements);
        for (VariableElement field : fields) {
            throw new ValidationFailure("Field injection is not supported", field);
        }
    }
}
