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
import io.jbock.simple.processor.binding.BuilderElement;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.FactoryElement;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;

import javax.annotation.processing.Generated;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class ComponentImpl {

    private static final String FACTORY_METHOD = "factory";
    private static final String BUILDER_METHOD = "builder";
    private static final String CREATE_METHOD = "create";
    private static final String MOCK_BUILDER_METHOD = "mockBuilder";

    private final ComponentElement component;
    private final Map<Key, NamedBinding> sorted;
    private final Function<Key, ParameterSpec> names;
    private final MockBuilder mockBuilder;
    private final Modifier[] modifiers;

    private ComponentImpl(
            ComponentElement component,
            Map<Key, NamedBinding> sorted,
            Function<Key, ParameterSpec> names,
            MockBuilder mockBuilder) {
        this.component = component;
        this.sorted = sorted;
        this.names = names;
        this.mockBuilder = mockBuilder;
        this.modifiers = component.element().getModifiers().stream()
                .filter(m -> m == PUBLIC).toArray(Modifier[]::new);
    }

    TypeSpec generate() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(component.generatedClass())
                .addModifiers(modifiers)
                .addSuperinterface(component.element().asType());
        spec.addFields(getFields());
        for (DependencyRequest r : component.requests()) {
            MethodSpec.Builder method = MethodSpec.methodBuilder(r.requestingElement().getSimpleName().toString());
            method.addStatement("return $L", sorted.get(r.key()).name());
            method.returns(r.key().typeName());
            method.addAnnotation(Override.class);
            method.addModifiers(PUBLIC);
            spec.addMethod(method.build());
        }
        component.factoryElement().ifPresent(factory -> {
            spec.addMethod(MethodSpec.methodBuilder(FACTORY_METHOD)
                    .addModifiers(STATIC)
                    .addModifiers(modifiers)
                    .returns(TypeName.get(factory.element().asType()))
                    .addStatement("return new $T()", factory.generatedClass())
                    .build());
            spec.addType(createFactoryImpl(factory));
        });
        component.builderElement().ifPresent(builder -> {
            spec.addMethod(MethodSpec.methodBuilder(BUILDER_METHOD)
                    .addModifiers(STATIC)
                    .addModifiers(modifiers)
                    .returns(TypeName.get(builder.element().asType()))
                    .addStatement("return new $T()", builder.generatedClass())
                    .build());
            spec.addType(createBuilderImpl(builder));
        });
        if (component.factoryElement().isEmpty() && component.builderElement().isEmpty()) {
            spec.addMethod(generateCreateMethod());
        }
        if (!component.omitMockBuilder()) {
            spec.addMethod(generateMockBuilderMethod());
            spec.addType(mockBuilder.generate());
        }
        spec.addAnnotation(AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("$S", SimpleComponentProcessor.class.getCanonicalName()))
                .addMember("comments", CodeBlock.of("$S", "https://github.com/jbock-java/simple-component"))
                .build());
        spec.addModifiers(FINAL);
        spec.addMethod(generateAllParametersConstructor());
        spec.addOriginatingElement(component.element());
        return spec.build();
    }

    private MethodSpec generateCreateMethod() {
        List<CodeBlock> constructorParameters = new ArrayList<>();
        MethodSpec.Builder method = MethodSpec.methodBuilder(CREATE_METHOD);
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            Key key = b.key();
            CodeBlock invocation = b.invocation(names);
            ParameterSpec param = names.apply(key);
            if (namedBinding.isComponentRequest()) {
                constructorParameters.add(CodeBlock.of("$N", names.apply(key)));
            }
            method.addStatement("$T $N = $L", key.typeName(), param, invocation);
        }
        return method
                .addModifiers(STATIC)
                .addModifiers(modifiers)
                .returns(TypeName.get(component.element().asType()))
                .addStatement("return new $T($L)",
                        component.generatedClass(),
                        constructorParameters.stream().collect(CodeBlock.joining(", ")))
                .build();
    }

    MethodSpec generateMockBuilderMethod() {
        MethodSpec.Builder method = MethodSpec.methodBuilder(MOCK_BUILDER_METHOD);
        method.addJavadoc("Visible for testing. Do not call this method from production code.");
        method.addStatement("return new $T()", mockBuilder.getClassName());
        method.returns(mockBuilder.getClassName());
        method.addModifiers(STATIC);
        if (component.generatePublicMockBuilder()) {
            method.addModifiers(modifiers);
        }
        return method.build();
    }

    private List<FieldSpec> getFields() {
        List<FieldSpec> fields = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            if (!namedBinding.isComponentRequest()) {
                continue;
            }
            TypeName type = namedBinding.binding().key().typeName();
            FieldSpec field = FieldSpec.builder(type, namedBinding.name(), PRIVATE, FINAL).build();
            fields.add(field);
        }
        return fields;
    }

    private MethodSpec generateAllParametersConstructor() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(PRIVATE);
        for (NamedBinding namedBinding : sorted.values()) {
            if (!namedBinding.isComponentRequest()) {
                continue;
            }
            ParameterSpec param = names.apply(namedBinding.binding().key());
            constructor.addParameter(param);
            constructor.addStatement("this.$1N = $1N", param);
        }
        return constructor.build();
    }

    private TypeSpec createFactoryImpl(FactoryElement factory) {
        TypeSpec.Builder spec = TypeSpec.classBuilder(factory.generatedClass());
        spec.addModifiers(PRIVATE, STATIC, FINAL);
        spec.addSuperinterface(factory.element().asType());
        ExecutableElement abstractMethod = factory.singleAbstractMethod();
        MethodSpec.Builder method = MethodSpec.methodBuilder(abstractMethod.getSimpleName().toString());
        method.addAnnotation(Override.class);
        method.addModifiers(abstractMethod.getModifiers().stream()
                .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
        method.returns(TypeName.get(component.element().asType()));
        List<CodeBlock> constructorParameters = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            Key key = b.key();
            CodeBlock invocation = b.invocation(names);
            ParameterSpec param = names.apply(key);
            if (namedBinding.isComponentRequest()) {
                constructorParameters.add(CodeBlock.of("$N", names.apply(key)));
            }
            if (b instanceof ParameterBinding) {
                method.addParameter(names.apply(b.key()));
            } else {
                method.addStatement("$T $N = $L", key.typeName(), param, invocation);
            }
        }
        method.addStatement("return new $T($L)", component.generatedClass(), constructorParameters.stream()
                .collect(CodeBlock.joining(", ")));
        spec.addMethod(method.build());
        return spec.build();
    }

    private TypeSpec createBuilderImpl(BuilderElement builder) {
        TypeMirror builderType = builder.element().asType();
        TypeSpec.Builder spec = TypeSpec.classBuilder(builder.generatedClass());
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder(builder.buildMethod().getSimpleName().toString());
        List<CodeBlock> constructorParameters = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            Key key = b.key();
            CodeBlock invocation = b.invocation(names);
            ParameterSpec param = names.apply(key);
            if (namedBinding.isComponentRequest()) {
                constructorParameters.add(CodeBlock.of("$N", names.apply(key)));
            }
            if (b instanceof ParameterBinding) {
                spec.addField(FieldSpec.builder(b.key().typeName(), names.apply(b.key()).name).build());
                MethodSpec.Builder setterMethod = MethodSpec.methodBuilder(b.element().getSimpleName().toString());
                setterMethod.addAnnotation(Override.class);
                setterMethod.addParameter(names.apply(b.key()));
                setterMethod.addStatement("this.$N = $N", names.apply(b.key()), names.apply(b.key()));
                setterMethod.addStatement("return this");
                setterMethod.returns(TypeName.get(builderType));
                setterMethod.addModifiers(b.element().getModifiers().stream()
                        .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
                spec.addMethod(setterMethod.build());
            } else {
                buildMethod.addStatement("$T $N = $L", key.typeName(), param, invocation);
            }
        }
        spec.addModifiers(PRIVATE, STATIC, FINAL);
        spec.addSuperinterface(builderType);
        buildMethod.addAnnotation(Override.class);
        buildMethod.addModifiers(builder.buildMethod().getModifiers().stream()
                .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
        buildMethod.returns(TypeName.get(component.element().asType()));
        buildMethod.addStatement("return new $T($L)", component.generatedClass(), constructorParameters.stream()
                .collect(CodeBlock.joining(", ")));
        spec.addMethod(buildMethod.build());
        return spec.build();
    }

    public static final class Factory {
        private final ComponentElement component;
        private final MockBuilder.Factory mockBuilderFactory;

        @Inject
        public Factory(ComponentElement component, MockBuilder.Factory mockBuilderFactory) {
            this.component = component;
            this.mockBuilderFactory = mockBuilderFactory;
        }

        ComponentImpl create(
                Map<Key, NamedBinding> sorted,
                Function<Key, ParameterSpec> names) {
            return new ComponentImpl(
                    component,
                    sorted,
                    names,
                    mockBuilderFactory.create(sorted, names));
        }
    }
}
