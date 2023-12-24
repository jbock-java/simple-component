package io.jbock.simple.processor.binding;

import io.jbock.javapoet.ClassName;
import io.jbock.simple.Component;
import io.jbock.simple.Inject;

import javax.lang.model.element.TypeElement;
import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;

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
}
