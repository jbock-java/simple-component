package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import jakarta.inject.Inject;

import javax.lang.model.element.Element;
import java.util.Map;
import java.util.Set;

public class InjectStep implements Step {

    @Override
    public Set<String> annotations() {
        return Set.of(Inject.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        return Set.of();
    }
}
