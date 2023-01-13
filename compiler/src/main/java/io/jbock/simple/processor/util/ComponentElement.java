package io.jbock.simple.processor.util;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.TypeName;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.Key;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.List;
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

    private final Supplier<List<DependencyRequest>> requests = memoize(() -> {
        List<ExecutableElement> methods = ElementFilter.methodsIn(element().getEnclosedElements());
        List<DependencyRequest> result = new ArrayList<>();
        for (ExecutableElement method : methods) {
            if (!method.getParameters().isEmpty()) {
                throw new ValidationFailure("The method may not have any parameters", method);
            }
            if (method.getModifiers().contains(Modifier.STATIC)) {
                throw new ValidationFailure("The method may not be static", method);
            }
            if (method.getReturnType().getKind() == TypeKind.VOID) {
                throw new ValidationFailure("The method may not return void", method);
            }
            result.add(new DependencyRequest(new Key(TypeName.get(method.getReturnType())), method));
        }
        return result;
    });

    private ComponentElement(TypeElement element) {
        this.element = element;
    }

    public static ComponentElement create(TypeElement element) {
        if (element.getKind() != ElementKind.INTERFACE) {
            throw new ValidationFailure("The component must be an interface", element);
        }
        return new ComponentElement(element);
    }
    
    public TypeElement element() {
        return element;
    }

    public List<DependencyRequest> getRequests() {
        return requests.get();
    }

    public ClassName generatedClass() {
        return generatedClass.get();
    }
}
