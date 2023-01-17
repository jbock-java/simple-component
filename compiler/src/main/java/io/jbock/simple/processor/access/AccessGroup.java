package io.jbock.simple.processor.access;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public final class AccessGroup {

    private final ClassName generatedClass;
    private final TypeElement typeElement;
    private final InjectBinding b;

    private AccessGroup(
            ClassName generatedClass,
            TypeElement typeElement,
            InjectBinding b) {
        this.generatedClass = generatedClass;
        this.typeElement = typeElement;
        this.b = b;
    }

    public static AccessGroup create(
            InjectBinding binding) {
        return new AccessGroup(binding.accessClassName(), binding.enclosingElement(), binding);
    }

    public TypeSpec generate() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(generatedClass);
        spec.addOriginatingElement(typeElement);
        spec.addModifiers(Modifier.PUBLIC);
        spec.addMethod(accessMethod());
        return spec.build();
    }

    private MethodSpec accessMethod() {
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
