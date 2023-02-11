package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProvidesStep implements Step {

    private final Messager messager;

    @Inject
    public ProvidesStep(
            Messager messager) {
        this.messager = messager;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(Provides.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        try {
            List<Element> elements = elementsByAnnotation.values().stream()
                    .flatMap(Set::stream)
                    .collect(Collectors.toList());
            List<ExecutableElement> methods = ElementFilter.methodsIn(elements);
            for (ExecutableElement m : methods) {
                if (!m.getModifiers().contains(Modifier.STATIC)) {
                    throw new ValidationFailure("The method must be static", m);
                }
                if (m.getReturnType().getKind() == TypeKind.VOID) {
                    throw new ValidationFailure("The method may not return void", m);
                }
                Element enclosing = m.getEnclosingElement();
                if (enclosing.getAnnotation(Component.class) == null) {
                    throw new ValidationFailure("The @Provides method must be nested inside a @Component", m);
                }
            }
        } catch (ValidationFailure f) {
            f.writeTo(messager);
        }
        return Set.of();
    }
}
