package io.jbock.simple.processor.graph;

import io.jbock.javapoet.TypeName;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.jbock.simple.processor.util.Visitors.EXECUTABLE_ELEMENT_VISITOR;
import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;
import static java.util.stream.Collectors.joining;

public class MissingBindingPrinter {

    public static final String INDENT = "    ";
    public static final String DOUBLE_INDENT = INDENT + INDENT;

    private final ComponentElement component;

    @Inject
    public MissingBindingPrinter(ComponentElement component) {
        this.component = component;
    }

    ValidationFailure fail(List<DependencyRequest> dependencyTrace) {
        List<DependencyRequest> trace = new ArrayList<>(dependencyTrace);
        Collections.reverse(trace);
        return failInternal(trace);
    }

    private ValidationFailure failInternal(List<DependencyRequest> trace) {
        DependencyRequest request = trace.get(0);
        StringBuilder message = new StringBuilder();
        message.append(request.key().toString()).append(" cannot be provided.");
        for (int i = 0; i < trace.size(); i++) {
            DependencyRequest r = trace.get(i);
            String formatted = format(r, i == trace.size() - 1 ? "requested" : "injected");
            message.append("\n").append(formatted);
        }
        return new ValidationFailure(message.toString(), component.element());
    }

    private String format(DependencyRequest request, String verb) {
        return INDENT
                + request.key()
                + " is "
                + verb
                + " at\n"
                + DOUBLE_INDENT
                + elementToString(request.requestElement());
    }

    private static String elementToString(ExecutableElement requestingElement) {
        StringBuilder result = enclosingTypeAndMemberName(requestingElement);
        result.append(EXECUTABLE_ELEMENT_VISITOR.visit(requestingElement).getParameters().stream()
                .map(parameter -> TypeName.get(parameter.asType()).toString())
                .collect(joining(", ", "(", ")")));
        return result.toString();
    }

    private static String enclosingElementToString(Element requestingElement) {
        if (!(requestingElement.getKind().isClass() || requestingElement.getKind().isInterface())) {
            return requestingElement.toString();
        }
        return TYPE_ELEMENT_VISITOR.visit(requestingElement).getSimpleName().toString();
    }

    private static StringBuilder enclosingTypeAndMemberName(ExecutableElement element) {
        StringBuilder name = new StringBuilder(enclosingElementToString(element.getEnclosingElement()));
        if (element.getKind() != ElementKind.CONSTRUCTOR) {
            name.append('.').append(getSimpleName(element));
        }
        return name;
    }

    public static String getSimpleName(Element element) {
        if (element.getKind().isInterface() || element.getKind().isClass()) {
            return TYPE_ELEMENT_VISITOR.visit(element).getSimpleName().toString();
        }
        if (element.getKind() == ElementKind.METHOD) {
            return EXECUTABLE_ELEMENT_VISITOR.visit(element).getSimpleName().toString();
        }
        if (element.getKind() == ElementKind.CONSTRUCTOR) {
            return "<init>";
        }
        return element.toString();
    }
}
