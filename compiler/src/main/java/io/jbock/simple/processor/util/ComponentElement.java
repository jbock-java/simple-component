package io.jbock.simple.processor.util;

import io.jbock.javapoet.ClassName;
import io.jbock.simple.Component;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.Key;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;

public final class ComponentElement {

    private final TypeElement element;
    private final TypeTool tool;
    private final Qualifiers qualifiers;

    private final Supplier<ClassName> generatedClass = memoize(() -> {
        ClassName className = ClassName.get(element());
        return className
                .topLevelClassName()
                .peerClass(String.join("_", className.simpleNames()) + "_Impl");
    });

    private final Supplier<Optional<FactoryElement>> factoryElement = memoize(() -> {
        for (Element el : element().getEnclosedElements()) {
            boolean hasFactoryAnnotation = el.getAnnotation(Component.Factory.class) != null;
            if (!hasFactoryAnnotation) {
                return Optional.empty();
            }
            TypeElement tel = Visitors.TYPE_ELEMENT_VISITOR.visit(el);
            if (tel == null) {
                return Optional.empty();
            }
            return Optional.of(new FactoryElement(tel, generatedClass(), qualifiers()));
        }
        return Optional.empty();
    });

    private final Supplier<List<ModuleElement>> modules = memoize(() -> element().getEnclosingElement().getEnclosedElements().stream()
            .filter(enclosed -> enclosed.getAnnotation(Component.Factory.class) != null)
            .map(Visitors.TYPE_ELEMENT_VISITOR::visit)
            .filter(Objects::nonNull)
            .map(el -> ModuleElement.create(el, tool()))
            .toList());

    private final Supplier<List<DependencyRequest>> requests = memoize(() -> {
        List<ExecutableElement> methods = ElementFilter.methodsIn(element().getEnclosedElements());
        List<DependencyRequest> result = new ArrayList<>();
        for (ExecutableElement method : methods) {
            if (!method.getParameters().isEmpty()) {
                throw new ValidationFailure("The method may not have any parameters", method);
            }
            if (method.getModifiers().contains(Modifier.DEFAULT)) {
                throw new ValidationFailure("Default method not allowed here", method);
            }
            if (method.getModifiers().contains(Modifier.STATIC)) {
                throw new ValidationFailure("The method may not be static", method);
            }
            if (method.getReturnType().getKind() == TypeKind.VOID) {
                throw new ValidationFailure("The method may not return void", method);
            }
            Key key = Key.create(method.getReturnType(), qualifiers().getQualifier(method));
            result.add(new DependencyRequest(key, method, qualifiers(), tool()));
        }
        return result;
    });

    private ComponentElement(TypeElement element, TypeTool tool, Qualifiers qualifiers) {
        this.element = element;
        this.tool = tool;
        this.qualifiers = qualifiers;
    }

    public static ComponentElement create(TypeElement element, TypeTool tool, Qualifiers qualifiers) {
        return new ComponentElement(element, tool, qualifiers);
    }

    public TypeElement element() {
        return element;
    }

    public Optional<FactoryElement> factoryElement() {
        return factoryElement.get();
    }

    public List<DependencyRequest> getRequests() {
        return requests.get();
    }

    public ClassName generatedClass() {
        return generatedClass.get();
    }

    public List<ModuleElement> modules() {
        return modules.get();
    }

    private TypeTool tool() {
        return tool;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentElement that = (ComponentElement) o;
        return element.equals(that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    private Qualifiers qualifiers() {
        return qualifiers;
    }
}
