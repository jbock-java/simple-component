package io.jbock.simple.processor.util;

import io.jbock.javapoet.ClassName;
import io.jbock.simple.Component;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;
import static javax.lang.model.util.ElementFilter.methodsIn;

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

    private final Supplier<Map<Key, DependencyRequest>> requests = memoize(() -> {
        List<ExecutableElement> methods = methodsIn(element().getEnclosedElements());
        Map<Key, DependencyRequest> result = new LinkedHashMap<>();
        for (ExecutableElement method : methods) {
            if (method.getModifiers().contains(Modifier.STATIC)) {
                continue; // ignore
            }
            if (!method.getParameters().isEmpty()) {
                throw new ValidationFailure("The method may not have any parameters", method);
            }
            if (method.getModifiers().contains(Modifier.DEFAULT)) {
                throw new ValidationFailure("Default method is not allowed here", method);
            }
            if (method.getReturnType().getKind() == TypeKind.VOID) {
                throw new ValidationFailure("The method may not return void", method);
            }
            Key key = Key.create(method.getReturnType(), qualifiers().getQualifier(method));
            result.put(key, new DependencyRequest(key, method, qualifiers(), tool()));
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

    public boolean isComponentRequest(Binding binding) {
        return requests.get().containsKey(binding.key());
    }

    public Collection<DependencyRequest> requests() {
        return requests.get().values();
    }

    public Map<Key, InjectBinding> providers() {
        List<ExecutableElement> methods = methodsIn(element.getEnclosedElements());
        Map<Key, InjectBinding> result = new LinkedHashMap<>();
        for (ExecutableElement method : methods) {
            if (method.getAnnotation(Provides.class) == null) {
                continue; // ignore
            }
            Key key = Key.create(method.getReturnType(), qualifiers.getQualifier(method));
            InjectBinding b = InjectBinding.createMethod(qualifiers, tool, method);
            result.put(key, b);
        }
        return result;
    }

    public ClassName generatedClass() {
        return generatedClass.get();
    }

    private TypeTool tool() {
        return tool;
    }

    private Qualifiers qualifiers() {
        return qualifiers;
    }
}
