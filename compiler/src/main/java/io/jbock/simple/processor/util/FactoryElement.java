package io.jbock.simple.processor.util;

import io.jbock.javapoet.ClassName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.STATIC;

public class FactoryElement {

    private final TypeElement element;
    private final ClassName parentClass;

    private final Supplier<ExecutableElement> singleAbstractMethod = Suppliers.memoize(() -> {
        List<ExecutableElement> methods = ElementFilter.methodsIn(element().getEnclosedElements());
        if (methods.isEmpty()) {
            throw new ValidationFailure("Factory method not found", element());
        }
        if (methods.size() != 1) {
            throw new ValidationFailure("Only one method allowed", element());
        }
        ExecutableElement method = methods.get(0);
        if (method.getModifiers().contains(STATIC)) {
            throw new ValidationFailure("The method may not be static", method);
        }
        if (method.getReturnType().getKind() == TypeKind.VOID) {
            throw new ValidationFailure("The method may not return void", method);
        }
        return method;
    });

    FactoryElement(TypeElement element, ClassName parentClass) {
        this.element = element;
        this.parentClass = parentClass;
    }

    public TypeElement element() {
        return element;
    }

    public ClassName generatedClass() {
        return parentClass.nestedClass(element().getSimpleName() + "_Impl");
    }

    public ExecutableElement singleAbstractMethod() {
        return singleAbstractMethod.get();
    }
}
