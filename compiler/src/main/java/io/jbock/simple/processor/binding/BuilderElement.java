package io.jbock.simple.processor.binding;

import io.jbock.javapoet.ClassName;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.jbock.simple.processor.util.Suppliers.memoize;
import static javax.lang.model.element.Modifier.STATIC;

public class BuilderElement {

    private final TypeElement element;
    private final ClassName parentClass;
    private final KeyFactory keyFactory;

    private final Supplier<ExecutableElement> buildMethod = memoize(() -> {
        Element componentElement = element().getEnclosingElement();
        List<ExecutableElement> methods = ElementFilter.methodsIn(element().getEnclosedElements()).stream()
                .filter(m -> !m.getModifiers().contains(STATIC))
                .filter(m -> qualifiers().tool().isSameType(m.getReturnType(), componentElement.asType()))
                .collect(Collectors.toList());
        if (methods.isEmpty()) {
            throw new ValidationFailure("Build method not found", element());
        }
        if (methods.size() != 1) {
            throw new ValidationFailure("Only one build method allowed", element());
        }
        ExecutableElement result = methods.get(0);
        if (!result.getParameters().isEmpty()) {
            throw new ValidationFailure("The build method may not have any parameters", result);
        }
        return result;
    });

    private final Supplier<List<ExecutableElement>> setterMethods = memoize(() -> {
        ExecutableElement buildMethod = buildMethod();
        List<ExecutableElement> result = ElementFilter.methodsIn(element().getEnclosedElements()).stream()
                .filter(m -> !m.getModifiers().contains(STATIC))
                .filter(m -> !Objects.equals(m, buildMethod))
                .collect(Collectors.toList());
        for (ExecutableElement m : result) {
            if (!qualifiers().tool().isSameType(m.getReturnType(), element().asType())) {
                throw new ValidationFailure("The setter method must return the builder type", m);
            }
            if (m.getParameters().size() != 1) {
                throw new ValidationFailure("The setter method must have exactly one parameter", m);
            }
        }
        return result;
    });

    BuilderElement(
            TypeElement element,
            ClassName parentClass,
            KeyFactory keyFactory) {
        this.element = element;
        this.parentClass = parentClass;
        this.keyFactory = keyFactory;
    }

    public TypeElement element() {
        return element;
    }

    public ClassName generatedClass() {
        return parentClass.nestedClass(element().getSimpleName() + "_Impl");
    }

    public ExecutableElement buildMethod() {
        return buildMethod.get();
    }

    List<ParameterBinding> parameterBindings() {
        return setterMethods().stream()
                .map(p -> ParameterBinding.create(p, qualifiers()))
                .collect(Collectors.toList());
    }

    List<ExecutableElement> setterMethods() {
        return setterMethods.get();
    }

    private KeyFactory qualifiers() {
        return keyFactory;
    }
}
