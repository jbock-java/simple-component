package io.jbock.simple.processor.writing;

import io.jbock.javapoet.AnnotationSpec;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.SimpleComponentProcessor;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.FactoryElement;

import javax.annotation.processing.Generated;
import javax.lang.model.element.ExecutableElement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            if (namedBinding.binding() instanceof InjectBinding) {
                InjectBinding b = (InjectBinding) namedBinding.binding();
                constructor.addStatement("this.$N = $L", field,
                        b.invokeExpression(b.dependencies().stream()
                                .map(d -> CodeBlock.of("$L", sorted.get(d.key()).name()))
                                .collect(CodeBlock.joining(", "))));
            } else if (namedBinding.binding() instanceof ParameterBinding) {
                ParameterBinding b = (ParameterBinding) namedBinding.binding();
                constructor.addStatement("this.$N = $N", field, b.parameterSpec());
            }
        }
        List<ParameterBinding> parameterBindings = component.factoryElement()
                .map(FactoryElement::parameterBindings)
                .orElse(List.of());
        for (DependencyRequest r : component.getRequests()) {
            MethodSpec.Builder method = MethodSpec.methodBuilder(r.requestingElement().getSimpleName().toString());
            method.addStatement("return $L", sorted.get(r.key()).name());
            method.returns(r.key().typeName());
            method.addAnnotation(Override.class);
            method.addModifiers(r.requestingElement().getModifiers().stream()
                    .filter(m -> m == PUBLIC || m == PROTECTED)
                    .collect(Collectors.toList()));
            spec.addMethod(method.build());
        }
        component.factoryElement().ifPresent(factory -> {
            spec.addMethod(MethodSpec.methodBuilder("factory")
                    .addModifiers(STATIC)
                    .returns(TypeName.get(factory.element().asType()))
                    .addStatement("return new $T()", factory.generatedClass())
                    .build());
            spec.addType(createFactory(parameterBindings, component, factory));
        });
        for (ParameterBinding b : parameterBindings) {
            constructor.addParameter(b.parameterSpec());
        }
        spec.addAnnotation(AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("$S", SimpleComponentProcessor.class.getCanonicalName()))
                .addMember("comments", CodeBlock.of("$S", "https://github.com/jbock-java/simple-component"))
                .build());
        spec.addModifiers(FINAL);
        spec.addMethod(constructor.build());
        spec.addOriginatingElement(component.element());
        return spec.build();
    }

    private static TypeSpec createFactory(
            List<ParameterBinding> parameterBindings,
            ComponentElement component,
            FactoryElement factory) {
        TypeSpec.Builder spec = TypeSpec.classBuilder(factory.generatedClass());
        spec.addModifiers(PRIVATE, STATIC, FINAL);
        spec.addSuperinterface(factory.element().asType());
        ExecutableElement abstractMethod = factory.singleAbstractMethod();
        MethodSpec.Builder method = MethodSpec.methodBuilder(abstractMethod.getSimpleName().toString());
        method.addAnnotation(Override.class);
        method.addModifiers(abstractMethod.getModifiers().stream()
                .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
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
