package io.jbock.simple.processor.writing;

import io.jbock.javapoet.AnnotationSpec;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.SimpleComponentProcessor;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.FactoryElement;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;

import javax.annotation.processing.Generated;
import javax.lang.model.element.ExecutableElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class ComponentImpl {

    private final ComponentElement component;

    @Inject
    public ComponentImpl(ComponentElement component) {
        this.component = component;
    }

    TypeSpec generate(Map<Key, NamedBinding> sorted) {
        TypeSpec.Builder spec = TypeSpec.classBuilder(component.generatedClass()).addSuperinterface(component.element().asType());
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(PRIVATE);
        Map<Key, ParameterSpec> parameterCache = new HashMap<>();
        Function<Key, ParameterSpec> names = key -> {
            return parameterCache.computeIfAbsent(key, k -> {
                String name = sorted.get(k).name();
                return ParameterSpec.builder(k.typeName(), name).build();
            });
        };
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            Key key = b.key();
            String name = namedBinding.name();
            if (namedBinding.isComponentRequest()) {
                FieldSpec field = FieldSpec.builder(key.typeName(), name, PRIVATE, FINAL).build();
                spec.addField(field);
                constructor.addStatement("this.$N = $L", field, b.invocation(names));
            } else if (!(b instanceof ParameterBinding)) {
                ParameterSpec param = names.apply(key);
                constructor.addStatement("$T $N = $L", b.key().typeName(), param, b.invocation(names));
            }
            if (b instanceof ParameterBinding) {
                constructor.addParameter(names.apply(key));
            }
        }
        for (DependencyRequest r : component.requests()) {
            MethodSpec.Builder method = MethodSpec.methodBuilder(r.requestingElement().getSimpleName().toString());
            method.addStatement("return $L", sorted.get(r.key()).name());
            method.returns(r.key().typeName());
            method.addAnnotation(Override.class);
            method.addModifiers(r.requestingElement().getModifiers().stream()
                    .filter(m -> m == PUBLIC || m == PROTECTED)
                    .collect(Collectors.toList()));
            spec.addMethod(method.build());
        }
        component.factoryElement().ifPresentOrElse(factory -> {
            spec.addMethod(MethodSpec.methodBuilder("factory")
                    .addModifiers(STATIC)
                    .returns(TypeName.get(factory.element().asType()))
                    .addStatement("return new $T()", factory.generatedClass())
                    .build());
            spec.addType(createFactory(factory, names));
        }, () -> {
            spec.addMethod(MethodSpec.methodBuilder("create")
                    .addModifiers(STATIC)
                    .returns(TypeName.get(component.element().asType()))
                    .addStatement("return new $T()", component.generatedClass())
                    .build());
        });
        spec.addAnnotation(AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("$S", SimpleComponentProcessor.class.getCanonicalName()))
                .addMember("comments", CodeBlock.of("$S", "https://github.com/jbock-java/simple-component"))
                .build());
        spec.addModifiers(FINAL);
        spec.addMethod(constructor.build());
        spec.addOriginatingElement(component.element());
        return spec.build();
    }

    private TypeSpec createFactory(
            FactoryElement factory,
            Function<Key, ParameterSpec> names) {
        Collection<ParameterBinding> parameterBindings = component.parameterBindings();
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
                .map(b -> CodeBlock.of("$N", names.apply(b.key())))
                .collect(CodeBlock.joining(", ")));
        for (ParameterBinding b : parameterBindings) {
            method.addParameter(names.apply(b.key()));
        }
        spec.addMethod(method.build());
        return spec.build();
    }
}
