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
import io.jbock.simple.processor.binding.KeyFactory;
import io.jbock.simple.processor.binding.ParameterBinding;

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

    private final KeyFactory keyFactory;
    private final ComponentElement component;
    private final Map<Key, NamedBinding> sorted;
    private final Function<Key, ParameterSpec> names;
    private final MockBuilder mockBuilder;
    private final BuilderImpl builderImpl;
    private final FactoryImpl factoryImpl;
    private final Modifier[] modifiers;

    @Inject
    public ComponentImpl(
            KeyFactory keyFactory,
            ComponentElement component,
            Context context,
            MockBuilder mockBuilder,
            BuilderImpl builderImpl,
            FactoryImpl factoryImpl) {
        this.keyFactory = keyFactory;
        this.component = component;
        this.sorted = context.sorted();
        this.names = context.names();
        this.modifiers = component.element().getModifiers().stream()
                .filter(m -> m == PUBLIC).toArray(Modifier[]::new);
        this.mockBuilder = mockBuilder;
        this.builderImpl = builderImpl;
        this.factoryImpl = factoryImpl;
    }

    public TypeSpec generate() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(component.generatedClass())
                .addModifiers(modifiers)
                .addModifiers(FINAL)
                .addSuperinterface(component.element().asType());
        spec.addFields(getFields());
        spec.addMethods(generateGetters());
        keyFactory.factoryElement().ifPresent(factory -> {
            spec.addMethod(generateFactoryMethod(factory));
            spec.addType(factoryImpl.generate(factory));
            if (component.mockBuilder()) {
                spec.addMethod(generateMockBuilderMethodFactory());
            }
        });
        keyFactory.builderElement().ifPresent(builder -> {
            spec.addMethod(generateBuilderMethod(builder));
            spec.addType(builderImpl.generate(builder, mockBuilder));
        });
        if (keyFactory.factoryElement().isEmpty() && keyFactory.builderElement().isEmpty()) {
            spec.addMethod(generateCreateMethod());
            if (component.mockBuilder()) {
                spec.addMethod(generateMockBuilderMethod());
            }
        }
        if (component.mockBuilder()) {
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

    private List<MethodSpec> generateGetters() {
        List<MethodSpec> result = new ArrayList<>(keyFactory.requests().size());
        for (DependencyRequest r : keyFactory.requests()) {
            MethodSpec.Builder method = MethodSpec.methodBuilder(r.requestingElement().getSimpleName().toString());
            method.addStatement("return $L", sorted.get(r.key()).name());
            method.returns(r.key().typeName());
            method.addAnnotation(Override.class);
            method.addModifiers(PUBLIC);
            result.add(method.build());
        }
        return result;
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
        spec.addStatement("return new $T()", factory.generatedClass());
        return spec.build();
    }

    private MethodSpec generateBuilderMethod(BuilderElement builder) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder(BUILDER_METHOD)
                .addModifiers(STATIC)
                .addModifiers(modifiers)
                .returns(builder.generatedClass());
        spec.addStatement("return new $T()", builder.generatedClass());
        return spec.build();
    }

    private MethodSpec generateCreateMethod() {
        List<CodeBlock> constructorParameters = new ArrayList<>();
        MethodSpec.Builder method = MethodSpec.methodBuilder(CREATE_METHOD);
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            Key key = b.key();
            CodeBlock invocation = b.invocation(names, sorted, true);
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
        if (component.publicMockBuilder()) {
            method.addModifiers(modifiers);
        }
        return method.build();
    }

    MethodSpec generateMockBuilderMethodFactory() {
        MethodSpec.Builder method = MethodSpec.methodBuilder(MOCK_BUILDER_METHOD);
        List<CodeBlock> constructorParameters = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            if (!(b instanceof ParameterBinding)) {
                continue;
            }
            ParameterSpec param = names.apply(b.key());
            constructorParameters.add(CodeBlock.of("$N", param));
        }
        if (component.publicMockBuilder()) {
            method.addModifiers(PUBLIC);
        }
        method.addParameters(factoryImpl.parameters());
        method.returns(mockBuilder.getClassName());
        method.addStatement("return new $T($L)", mockBuilder.getClassName(),
                constructorParameters.stream().collect(CodeBlock.joining(", ")));
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
}
