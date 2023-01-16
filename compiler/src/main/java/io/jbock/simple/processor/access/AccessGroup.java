package io.jbock.simple.processor.access;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Optional;

public final class AccessGroup {

    private final ClassName generatedClass;
    private final TypeElement typeElement;
    private final List<InjectBinding> bindings;

    private AccessGroup(
            ClassName generatedClass,
            TypeElement typeElement,
            List<InjectBinding> bindings) {
        this.generatedClass = generatedClass;
        this.typeElement = typeElement;
        this.bindings = bindings;
    }

    static AccessGroup create(
            TypeElement typeElement,
            List<InjectBinding> bindings) {
        return new AccessGroup(bindings.get(0).accessClassName(), typeElement, bindings);
    }

    public TypeSpec generate() {
        Optional<InjectBinding> constructorBinding = bindings.stream().filter(b -> b.element().getKind() == ElementKind.CONSTRUCTOR).findAny();
        if (constructorBinding.isPresent()) {
            InjectBinding cb = constructorBinding.orElseThrow();
            if (bindings.size() >= 2) {
                bindings.stream()
                        .filter(b -> b.element().getKind() == ElementKind.METHOD)
                        .findFirst()
                        .ifPresentOrElse(
                                mb -> {
                                    throw new ValidationFailure(
                                            "Static method bindings are not allowed in a class with a constructor binding",
                                            mb.element());
                                },
                                () -> {
                                    throw new ValidationFailure(
                                            "Only one constructor binding per class allowed",
                                            cb.element());
                                });
            }
        }
        TypeSpec.Builder spec = TypeSpec.classBuilder(generatedClass);
        spec.addOriginatingElement(typeElement);
        spec.addModifiers(Modifier.PUBLIC);
        for (InjectBinding b : bindings) {
            spec.addMethod(accessMethod(b));
        }
        return spec.build();
    }

    private MethodSpec accessMethod(InjectBinding b) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder(b.accessMethodName());
        spec.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        if (b.element().getKind() == ElementKind.CONSTRUCTOR) {
            spec.returns(TypeName.get(b.element().getEnclosingElement().asType()));
        } else {
            spec.returns(TypeName.get(b.element().getReturnType()));
        }
        for (DependencyRequest d : b.dependencies()) {
            if (d.requestingElement() instanceof VariableElement v) {
                spec.addParameter(ParameterSpec.builder(TypeName.get(v.asType()), v.getSimpleName().toString()).build());
            } else {
                throw new AssertionError("All dependencies of an InjectBinding should be VariableElements");
            }
        }
        spec.addStatement("return $L", b.invokeExpression(b.dependencies().stream()
                .map(d -> CodeBlock.of("$L", d.requestingElement().getSimpleName()))
                .collect(CodeBlock.joining(", "))));
        return spec.build();
    }

    public ClassName generatedClass() {
        return generatedClass;
    }
}
