package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.Binds;
import io.jbock.simple.SimpleModule;
import io.jbock.simple.processor.util.ExecutableElementValidator;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BindsStep implements Step {

    private final Messager messager;
    private final ExecutableElementValidator executableElementValidator;

    public BindsStep(Messager messager, ExecutableElementValidator executableElementValidator) {
        this.messager = messager;
        this.executableElementValidator = executableElementValidator;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(Binds.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        try {
            List<Element> elements = elementsByAnnotation.values().stream().flatMap(Set::stream).toList();
            List<ExecutableElement> methods = ElementFilter.methodsIn(elements);
            for (ExecutableElement m : methods) {
                executableElementValidator.validate(m);
                if (m.getEnclosingElement().getAnnotation(SimpleModule.class) == null) {
                    throw new ValidationFailure("A class containing @Binds methods must have the @SimpleModule annotation", m.getEnclosingElement());
                }
            }
        } catch (ValidationFailure f) {
            f.writeTo(messager);
        }
        return Set.of();
    }
}
