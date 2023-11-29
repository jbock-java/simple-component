package io.jbock.simple.processor.binding;

import io.jbock.javapoet.ClassName;
import io.jbock.simple.Component;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.util.Visitors;

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
    private final KeyFactory keyFactory;

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
                continue;
            }
            TypeElement tel = Visitors.TYPE_ELEMENT_VISITOR.visit(el);
            if (tel == null) {
                continue;
            }
            return Optional.of(new FactoryElement(tel, generatedClass(), keyFactory()));
        }
        return Optional.empty();
    });


    private final Supplier<Optional<BuilderElement>> builderElement = memoize(() -> {
        for (Element el : element().getEnclosedElements()) {
            boolean hasBuilderAnnotation = el.getAnnotation(Component.Builder.class) != null;
            if (!hasBuilderAnnotation) {
                continue;
            }
            TypeElement tel = Visitors.TYPE_ELEMENT_VISITOR.visit(el);
            if (tel == null) {
                continue;
            }
            return Optional.of(new BuilderElement(tel, generatedClass(), keyFactory()));
        }
        return Optional.empty();
    });

    private final Supplier<Map<Key, InjectBinding>> providesBindings = memoize(() -> {
        List<ExecutableElement> methods = methodsIn(element().getEnclosedElements());
        Map<Key, InjectBinding> result = new LinkedHashMap<>();
        for (ExecutableElement method : methods) {
            if (method.getAnnotation(Provides.class) == null) {
                continue; // ignore
            }
            Key key = keyFactory().getKey(method);
            InjectBinding b = InjectBinding.create(keyFactory(), method);
            result.put(key, b);
        }
        return result;
    });

    private final Supplier<Map<Key, DependencyRequest>> requests = memoize(() -> {
        List<ExecutableElement> methods = methodsIn(element().getEnclosedElements());
        Map<Key, DependencyRequest> result = new LinkedHashMap<>();
        for (ExecutableElement method : methods) {
            if (method.getModifiers().contains(Modifier.STATIC)) {
                continue; // ignore
            }
            if (method.getAnnotation(Provides.class) != null) {
                continue; // ignore
            }
            if (!method.getParameters().isEmpty()) {
                throw new ValidationFailure("Request method may not have any parameters", method);
            }
            if (method.getModifiers().contains(Modifier.DEFAULT)) {
                throw new ValidationFailure("Default modifier is not allowed here", method);
            }
            if (method.getReturnType().getKind() == TypeKind.VOID) {
                throw new ValidationFailure("Request method may not return void", method);
            }
            Key key = keyFactory().getKey(method);
            result.put(key, new DependencyRequest(key, method, method));
        }
        return result;
    });

    private final Supplier<Map<Key, ParameterBinding>> parameterBindings = memoize(() -> {
        List<ParameterBinding> pBindings = factoryElement()
                .map(FactoryElement::parameterBindings)
                .or(() -> builderElement().map(BuilderElement::parameterBindings))
                .orElse(List.of());
        Map<Key, ParameterBinding> result = new LinkedHashMap<>();
        for (ParameterBinding b : pBindings) {
            ParameterBinding previousBinding = result.put(b.key(), b);
            if (previousBinding != null) {
                Element p = previousBinding.element();
                throw new ValidationFailure("The binding is in conflict with another parameter: " +
                        p.asType() + ' ' + p.getSimpleName(), b.element());
            }
        }
        return result;
    });

    private ComponentElement(
            TypeElement element,
            KeyFactory keyFactory) {
        this.element = element;
        this.keyFactory = keyFactory;
    }

    public static ComponentElement create(
            TypeElement element,
            KeyFactory keyFactory) {
        return new ComponentElement(element, keyFactory);
    }

    public TypeElement element() {
        return element;
    }

    public Optional<FactoryElement> factoryElement() {
        return factoryElement.get();
    }

    public Optional<BuilderElement> builderElement() {
        return builderElement.get();
    }

    public boolean isComponentRequest(Binding binding) {
        return requests.get().containsKey(binding.key());
    }

    public Collection<DependencyRequest> requests() {
        return requests.get().values();
    }

    public Map<Key, InjectBinding> providesBindings() {
        return providesBindings.get();
    }

    public ClassName generatedClass() {
        return generatedClass.get();
    }

    private KeyFactory keyFactory() {
        return keyFactory;
    }

    public Optional<Binding> parameterBinding(Key key) {
        return Optional.ofNullable(parameterBindings.get().get(key));
    }

    public Collection<ParameterBinding> parameterBindings() {
        return parameterBindings.get().values();
    }

    public boolean generatePublicMockBuilder() {
        Component annotation = element.getAnnotation(Component.class);
        if (annotation == null) {
            return false;
        }
        return annotation.generatePublicMockBuilder();
    }

    public boolean omitMockBuilder() {
        Component annotation = element.getAnnotation(Component.class);
        if (annotation == null) {
            return false;
        }
        return annotation.omitMockBuilder();
    }
}
