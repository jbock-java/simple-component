package io.jbock.simple.processor.util;

import io.jbock.javapoet.ClassName;
import io.jbock.simple.processor.binding.ParameterBinding;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.jbock.simple.processor.util.Suppliers.memoize;
import static javax.lang.model.element.Modifier.STATIC;

public class FactoryElement {

    private final TypeElement element;
    private final ClassName parentClass;
    private final Qualifiers qualifiers;

    private final Supplier<ExecutableElement> singleAbstractMethod = memoize(() -> {
        List<ExecutableElement> methods = ElementFilter.methodsIn(element().getEnclosedElements())
                .stream().filter(m -> !m.getModifiers().contains(STATIC))
                .collect(Collectors.toList());
        if (methods.isEmpty()) {
            throw new ValidationFailure("Factory method not found", element());
        }
        if (methods.size() != 1) {
            throw new ValidationFailure("Only one method allowed", element());
        }
        ExecutableElement method = methods.get(0);
        if (method.getReturnType().getKind() == TypeKind.VOID) {
            throw new ValidationFailure("The method may not return void", method);
        }
        return method;
    });

    private final Supplier<List<ParameterBinding>> parameterBindings = memoize(() ->
            singleAbstractMethod().getParameters().stream()
                    .map(p -> ParameterBinding.create(p, qualifiers()))
                    .collect(Collectors.toList()));

    FactoryElement(
            TypeElement element,
            ClassName parentClass,
            Qualifiers qualifiers) {
        this.element = element;
        this.parentClass = parentClass;
        this.qualifiers = qualifiers;
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

    public List<ParameterBinding> parameterBindings() {
        return parameterBindings.get();
    }

    private Qualifiers qualifiers() {
        return qualifiers;
    }
}
