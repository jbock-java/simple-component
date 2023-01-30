package io.jbock.simple.processor.binding;

import io.jbock.simple.processor.util.Qualifiers;
import io.jbock.simple.processor.util.TypeTool;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;
import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;

public final class DependencyRequest {

    private final Key key;
    private final Element requestingElement;
    private final Qualifiers qualifiers;
    private final TypeTool tool;

    private final Supplier<Optional<InjectBinding>> binding = memoize(() -> keyElement().flatMap(element -> {
        TypeElement typeElement = TYPE_ELEMENT_VISITOR.visit(element);
        if (typeElement == null) {
            return Optional.empty();
        }
        Map<Key, InjectBinding> m = qualifiers().injectBindings(typeElement);
        return Optional.ofNullable(m.get(key()));
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
