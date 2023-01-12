package io.jbock.simple.processor.binding;

import io.jbock.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;

public final class InjectBinding implements Binding {
    
    private final Key key;
    private final ExecutableElement bindingElement;

    private final Supplier<List<DependencyRequest>> dependencies = memoize(() -> {
        List<DependencyRequest> result = new ArrayList<>();
        for (VariableElement parameter : bindingElement().getParameters()) {
            result.add(new DependencyRequest(new Key(TypeName.get(parameter.asType())), parameter));
        }
        return result;
    });

    public InjectBinding(
            Key key,
            ExecutableElement bindingElement) {
        this.key = key;
        this.bindingElement = bindingElement;
    }

    public Key key() {
        return key;
    }

    public ExecutableElement bindingElement() {
        return bindingElement;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InjectBinding) obj;
        return Objects.equals(this.key, that.key) &&
                Objects.equals(this.bindingElement, that.bindingElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, bindingElement);
    }

    public List<DependencyRequest> dependencies() {
        return dependencies.get();
    }

    @Override
    public String toString() {
        return "InjectBinding[" + "" + key + ", " + "" + bindingElement + ']';
    }
}
