package io.jbock.simple.processor.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.FactoryElement;

import javax.lang.model.element.ExecutableElement;
import java.util.Map;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class ComponentImpl {

    TypeSpec generate(
            ComponentElement component,
            Map<Key, NamedBinding> sorted) {
        TypeSpec.Builder spec = TypeSpec.classBuilder(component.generatedClass()).addSuperinterface(component.element().asType());
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(PRIVATE);
        if (component.factoryElement().isEmpty()) {
            spec.addMethod(MethodSpec.methodBuilder("create")
                    .addModifiers(STATIC)
                    .returns(TypeName.get(component.element().asType()))
                    .addStatement("return new $T()", component.generatedClass())
                    .build());
        }
        for (Map.Entry<Key, NamedBinding> e : sorted.entrySet()) {
            InjectBinding b = e.getValue().binding();
            Key key = b.key();
            String uniqueName = e.getValue().name();
            FieldSpec field = FieldSpec.builder(
                    key.typeName(), uniqueName, PRIVATE, FINAL).build();
            spec.addField(field);
            constructor.addStatement("this.$N = $L", field,
                    b.invokeExpression(b.dependencies().stream()
                            .map(d -> CodeBlock.of("$L", sorted.get(d.key()).name()))
                            .collect(CodeBlock.joining(", "))));
        }
        for (DependencyRequest r : component.getRequests()) {
            MethodSpec.Builder method = MethodSpec.methodBuilder(r.requestElement().getSimpleName().toString());
            method.addStatement("return $L", sorted.get(r.key()).name());
            method.returns(r.key().typeName());
            method.addAnnotation(Override.class);
            method.addModifiers(r.requestElement().getModifiers().stream()
                    .filter(m -> m == PUBLIC || m == PROTECTED).toList());
            spec.addMethod(method.build());
        }
        component.factoryElement().ifPresent(factory -> {
            spec.addMethod(MethodSpec.methodBuilder("factory")
                    .returns(TypeName.get(factory.element().asType()))
                    .addStatement("return new $T()", factory.generatedClass())
                    .build());
            spec.addType(createFactory(component, factory));
        });
        return spec.addMethod(constructor.build())
                .addOriginatingElement(component.element()).build();
    }

    private static TypeSpec createFactory(
            ComponentElement component,
            FactoryElement factory) {
        TypeSpec.Builder spec = TypeSpec.classBuilder(factory.generatedClass());
        spec.addSuperinterface(factory.element().asType());
        ExecutableElement method = factory.singleAbstractMethod();
        spec.addMethod(MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addAnnotation(Override.class)
                .addModifiers(method.getModifiers().stream()
                        .filter(m -> m == PUBLIC || m == PROTECTED).toList())
                .returns(TypeName.get(component.element().asType()))
                .addStatement("return new $T()", component.generatedClass())
                .build());
        return spec.build();
    }
}
