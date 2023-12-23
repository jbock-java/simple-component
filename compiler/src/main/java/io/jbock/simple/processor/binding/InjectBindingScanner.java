package io.jbock.simple.processor.binding;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.Util;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.jbock.simple.processor.util.Visitors.EXECUTABLE_ELEMENT_VISITOR;

public class InjectBindingScanner {

    private final TypeTool tool;

    @Inject
    public InjectBindingScanner(TypeTool tool) {
        this.tool = tool;
    }

    public List<ExecutableElement> scan(TypeElement typeElement) {
        List<ExecutableElement> result = new ArrayList<>();
        for (TypeElement element : Util.getWithEnclosing(typeElement)) {
            tool.elements().getAllMembers(element).stream()
                    .filter(tool::hasInjectAnnotation)
                    .map(EXECUTABLE_ELEMENT_VISITOR::visit)
                    .filter(Objects::nonNull)
                    .forEach(result::add);
        }
        return result;
    }
}
