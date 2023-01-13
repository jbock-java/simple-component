package io.jbock.simple.processor.binding;

import io.jbock.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static io.jbock.simple.processor.util.Suppliers.memoize;

public final class InjectBinding extends Binding {

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
        super(key);
        this.bindingElement = bindingElement;
    }

    public ExecutableElement bindingElement() {
        return bindingElement;
    }

    public List<DependencyRequest> dependencies() {
        return dependencies.get();
    }

    @Override
    public String toString() {
        return "InjectBinding[" + "" + key() + ']';
    }
}
