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

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
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
    private final BuilderImpl builderImpl;
    private final FactoryImpl factoryImpl;
    private final Modifier[] modifiers;

    private ComponentImpl(
            ComponentElement component,
            Map<Key, NamedBinding> sorted,
            Function<Key, ParameterSpec> names,
            MockBuilder mockBuilder,
            BuilderImpl builderImpl,
            FactoryImpl factoryImpl) {
        this.component = component;
        this.sorted = sorted;
        this.names = names;
        this.mockBuilder = mockBuilder;
        this.modifiers = component.element().getModifiers().stream()
                .filter(m -> m == PUBLIC).toArray(Modifier[]::new);
        this.builderImpl = builderImpl;
        this.factoryImpl = factoryImpl;
    }

    TypeSpec generate() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(component.generatedClass())
                .addModifiers(modifiers)
                .addModifiers(FINAL)
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
            spec.addMethod(generateFactoryMethod(factory));
            spec.addType(factoryImpl.generate(factory, mockBuilder));
        });
        component.builderElement().ifPresent(builder -> {
            spec.addMethod(generateBuilderMethod(builder));
            spec.addType(builderImpl.generate(builder, mockBuilder));
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
                .addMember("comments", CodeBlock.of("$S", getComments()))
                .build());
        spec.addMethod(generateAllParametersConstructor());
        spec.addOriginatingElement(component.element());
        return spec.build();
    }

    private String getComments() {
        String version = Objects.toString(getClass().getPackage().getImplementationVersion(), "");
        return "https://github.com/jbock-java/simple-component" + (version.isEmpty() ? "" : " " + version);
    }

    private MethodSpec generateFactoryMethod(FactoryElement factory) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder(FACTORY_METHOD)
                .addModifiers(STATIC)
                .addModifiers(modifiers)
                .returns(TypeName.get(factory.element().asType()));
        if (component.omitMockBuilder()) {
            spec.addStatement("return new $T()", factory.generatedClass());
        } else {
            spec.addStatement("return new $T(null)", factory.generatedClass());
        }
        return spec.build();
    }

    private MethodSpec generateBuilderMethod(BuilderElement builder) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder(BUILDER_METHOD)
                .addModifiers(STATIC)
                .addModifiers(modifiers)
                .returns(TypeName.get(builder.element().asType()));
        if (component.omitMockBuilder()) {
            spec.addStatement("return new $T()", builder.generatedClass());
        } else {
            spec.addStatement("return new $T(null)", builder.generatedClass());
        }
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

    public static final class Factory {
        private final ComponentElement component;
        private final MockBuilder.Factory mockBuilderFactory;
        private final BuilderImpl.Factory builderImplFactory;
        private final FactoryImpl.Factory factoryImplFactory;

        @Inject
        public Factory(
                ComponentElement component,
                MockBuilder.Factory mockBuilderFactory,
                BuilderImpl.Factory builderImplFactory,
                FactoryImpl.Factory factoryImplFactory) {
            this.component = component;
            this.mockBuilderFactory = mockBuilderFactory;
            this.builderImplFactory = builderImplFactory;
            this.factoryImplFactory = factoryImplFactory;
        }

        ComponentImpl create(
                Map<Key, NamedBinding> sorted,
                Function<Key, ParameterSpec> names) {
            return new ComponentImpl(
                    component,
                    sorted,
                    names,
                    mockBuilderFactory.create(sorted, names),
                    builderImplFactory.create(sorted, names),
                    factoryImplFactory.create(sorted, names));
        }
    }
}
