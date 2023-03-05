package io.jbock.simple.processor.util;

import io.jbock.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

import static io.jbock.simple.processor.util.Visitors.EXECUTABLE_ELEMENT_VISITOR;
import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;
import static java.util.stream.Collectors.joining;

public final class Printing {

    public static final String INDENT = "    ";
    public static final String DOUBLE_INDENT = INDENT + INDENT;

    public static String elementToString(Element element) {
        if (element.getKind() == ElementKind.PARAMETER) {
            return Visitors.PARAMETER_VISITOR.visit(element).getSimpleName().toString();
        }
        ExecutableElement executableElement = EXECUTABLE_ELEMENT_VISITOR.visit(element);
        if (executableElement == null) {
            throw new UnsupportedOperationException("Can't determine string for element " + element);
        }
        StringBuilder result = enclosingTypeAndMemberName(executableElement);
        result.append(executableElement.getParameters().stream()
                .map(parameter -> TypeName.get(parameter.asType()).toString())
                .collect(joining(", ", "(", ")")));
        return result.toString();
    }

    private static String enclosingElementToString(Element element) {
        if (!(element.getKind().isClass() || element.getKind().isInterface())) {
            return element.toString();
        }
        return TYPE_ELEMENT_VISITOR.visit(element).getSimpleName().toString();
    }

    private static StringBuilder enclosingTypeAndMemberName(ExecutableElement element) {
        StringBuilder name = new StringBuilder(enclosingElementToString(element.getEnclosingElement()));
        if (element.getKind() != ElementKind.CONSTRUCTOR) {
            name.append('.').append(getSimpleName(element));
        }
        return name;
    }

    private static String getSimpleName(Element element) {
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

    private Printing() {
    }
}
