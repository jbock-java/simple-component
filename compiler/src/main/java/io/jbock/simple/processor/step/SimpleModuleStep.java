package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.SimpleModule;
import io.jbock.simple.processor.util.TypeElementValidator;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleModuleStep implements Step {

    private final TypeElementValidator validator;
    private final Messager messager;

    public SimpleModuleStep(TypeElementValidator validator, Messager messager) {
        this.validator = validator;
        this.messager = messager;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(SimpleModule.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        try {
            List<Element> elements = elementsByAnnotation.values().stream().flatMap(Set::stream).toList();
            List<TypeElement> typeElements = ElementFilter.typesIn(elements);
            for (TypeElement typeElement : typeElements) {
                validator.validate(typeElement);
            }
        } catch (ValidationFailure failure) {
            failure.writeTo(messager);
        }
        return Set.of();
    }
}
