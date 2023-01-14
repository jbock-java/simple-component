package io.jbock.simple.processor.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.FactoryElement;

import javax.lang.model.element.ExecutableElement;
import java.util.List;
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
            NamedBinding namedBinding = e.getValue();
            Key key = namedBinding.binding().key();
            String name = namedBinding.name();
            FieldSpec field = FieldSpec.builder(key.typeName(), name, PRIVATE, FINAL).build();
            spec.addField(field);
            if (namedBinding.binding() instanceof InjectBinding b) {
                constructor.addStatement("this.$N = $L", field,
                        b.invokeExpression(namedBinding.binding().dependencies().stream()
                                .map(d -> CodeBlock.of("$L", sorted.get(d.key()).name()))
                                .collect(CodeBlock.joining(", "))));
            } else if (namedBinding.binding() instanceof ParameterBinding b) {
                constructor.addStatement("this.$N = $N", field, b.parameterSpec());
            }
        }
        List<ParameterBinding> parameterBindings = component.factoryElement()
                .map(FactoryElement::parameterBindings)
                .orElse(List.of());
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
            spec.addType(createFactory(parameterBindings, component, factory));
        });
        for (ParameterBinding b : parameterBindings) {
            constructor.addParameter(b.parameterSpec());
        }
        return spec.addMethod(constructor.build())
                .addOriginatingElement(component.element()).build();
    }

    private static TypeSpec createFactory(
            List<ParameterBinding> parameterBindings,
            ComponentElement component,
            FactoryElement factory) {
        TypeSpec.Builder spec = TypeSpec.classBuilder(factory.generatedClass());
        spec.addSuperinterface(factory.element().asType());
        ExecutableElement abstractMethod = factory.singleAbstractMethod();
        MethodSpec.Builder method = MethodSpec.methodBuilder(abstractMethod.getSimpleName().toString());
        method.addAnnotation(Override.class);
        method.addModifiers(abstractMethod.getModifiers().stream()
                .filter(m -> m == PUBLIC || m == PROTECTED).toList());
        method.returns(TypeName.get(component.element().asType()));
        method.addStatement("return new $T($L)", component.generatedClass(), parameterBindings.stream()
                .map(b -> CodeBlock.of("$N", b.parameterSpec()))
                .collect(CodeBlock.joining(", ")));
        for (ParameterBinding b : parameterBindings) {
            method.addParameter(b.parameterSpec());
        }
        spec.addMethod(method.build());
        return spec.build();
    }
}
