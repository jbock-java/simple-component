package io.jbock.simple.processor.binding;

import io.jbock.simple.processor.util.Qualifiers;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.util.Visitors;
import jakarta.inject.Inject;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;

public final class DependencyRequest {

    private final Key key;
    private final Element requestingElement;
    private final Qualifiers qualifiers;
    private final TypeTool tool;

    private final Supplier<Optional<InjectBinding>> binding = memoize(() -> keyElement().flatMap(element -> {
        TypeElement typeElement = Visitors.TYPE_ELEMENT_VISITOR.visit(element);
        if (typeElement == null) {
            return Optional.empty();
        }
        List<? extends Element> allMembers = tool().elements().getAllMembers(typeElement);
        List<ExecutableElement> constructors = ElementFilter.constructorsIn(allMembers).stream()
                .filter(c -> c.getAnnotationMirrors().stream().anyMatch(m -> tool().isSameType(m.getAnnotationType(), Inject.class)))
                .toList();
        List<ExecutableElement> methods = ElementFilter.methodsIn(allMembers).stream()
                .filter(c -> c.getAnnotationMirrors().stream().anyMatch(m -> tool().isSameType(m.getAnnotationType(), Inject.class)))
                .toList();
        if (constructors.isEmpty() && methods.isEmpty()) {
            return Optional.empty();
        }
        if (constructors.size() >= 2) {
            throw new ValidationFailure("Only one constructor binding per class allowed",
                    element);
        }
        if (methods.size() >= 2) {
            throw new ValidationFailure("Only one static method binding per class allowed",
                    element);
        }
        if (!constructors.isEmpty() && !methods.isEmpty()) {
            throw new ValidationFailure(
                    "Static method bindings are not allowed in a class with a constructor binding",
                    element);
        }
        if (!constructors.isEmpty()) {
            return Optional.of(InjectBinding.createConstructor(qualifiers(), tool(), constructors.get(0)));
        }
        return Optional.of(InjectBinding.createMethod(qualifiers(), tool(), methods.get(0)));
    }));

    public DependencyRequest(
            Key key,
            Element requestingElement,
            Qualifiers qualifiers,
            TypeTool tool) {
        this.key = key;
        this.requestingElement = requestingElement;
        this.qualifiers = qualifiers;
        this.tool = tool;
    }

    public Key key() {
        return key;
    }

    public Element requestingElement() {
        return requestingElement;
    }

    public Optional<InjectBinding> binding() {
        return binding.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DependencyRequest) obj;
        return Objects.equals(this.key, that.key) &&
                Objects.equals(this.requestingElement, that.requestingElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, requestingElement);
    }

    @Override
    public String toString() {
        return "[" +
                "key=" + key + ", " +
                "requestingElement=" + requestingElement + ']';
    }

    private TypeTool tool() {
        return tool;
    }

    private Qualifiers qualifiers() {
        return qualifiers;
    }

    private Optional<Element> keyElement() {
        return tool().types().asElement(key().type());
    }
}
