package io.jbock.simple.processor.binding;

import io.jbock.javapoet.ClassName;
import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.util.Visitors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.jbock.simple.processor.util.Suppliers.memoize;
import static io.jbock.simple.processor.util.Visitors.ANNOTATION_VALUE_AS_TYPE;
import static io.jbock.simple.processor.util.Visitors.DECLARED_TYPE_VISITOR;
import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;

public final class ComponentElement {

    private final TypeElement element;

    private final Supplier<ClassName> generatedClass = memoize(() -> {
        ClassName className = ClassName.get(element());
        return className
                .topLevelClassName()
                .peerClass(String.join("_", className.simpleNames()) + "_Impl");
    });

    @Inject
    public ComponentElement(
            TypeElement element) {
        this.element = element;
    }

    public TypeElement element() {
        return element;
    }

    public ClassName generatedClass() {
        return generatedClass.get();
    }

    public boolean publicMockBuilder() {
        Component annotation = element.getAnnotation(Component.class);
        if (annotation == null) {
            return false;
        }
        return annotation.publicMockBuilder();
    }

    public boolean mockBuilder() {
        Component annotation = element.getAnnotation(Component.class);
        if (annotation == null) {
            return false;
        }
        return annotation.mockBuilder();
    }

    public List<TypeElement> modules() {
        return modules.get();
    }

    private final Supplier<List<TypeElement>> modules = memoize(() -> {
        List<? extends AnnotationMirror> annotationMirrors = element().getAnnotationMirrors();
        AnnotationMirror annotationMirror = annotationMirrors.stream()
                .filter(mirror -> {
                    TypeElement tel = Visitors.TYPE_ELEMENT_VISITOR.visit(mirror.getAnnotationType().asElement());
                    if (tel == null) {
                        return false;
                    }
                    return tel.getQualifiedName().contentEquals(Component.class.getCanonicalName());
                })
                .findFirst()
                .orElseThrow(AssertionError::new);
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                annotationMirror.getElementValues();
        return elementValues.entrySet().stream()
                .filter(e -> "modules".contentEquals(e.getKey().getSimpleName()))
                .map(Map.Entry::getValue)
                .flatMap(m -> Visitors.ANNOTATION_VALUE_AS_ARRAY.visit(m, null).stream())
                .map(ANNOTATION_VALUE_AS_TYPE::visit)
                .filter(Objects::nonNull)
                .map(DECLARED_TYPE_VISITOR::visit)
                .filter(Objects::nonNull)
                .map(DeclaredType::asElement)
                .filter(Objects::nonNull)
                .map(TYPE_ELEMENT_VISITOR::visit)
                .collect(Collectors.toList());
    });


}
