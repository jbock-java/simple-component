package io.jbock.simple.processor.util;

import io.jbock.javapoet.ClassName;

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

    private ComponentElement(TypeElement element) {
        this.element = element;
    }

    public static ComponentElement create(TypeElement element) {
        return new ComponentElement(element);
    }
    
    public TypeElement element() {
        return element;
    }

    public ClassName generatedClass() {
        return generatedClass.get();
    }
}
