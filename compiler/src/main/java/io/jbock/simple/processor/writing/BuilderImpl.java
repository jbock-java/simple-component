package io.jbock.simple.processor.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.BuilderElement;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;

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

public class BuilderImpl {

    private final ComponentElement component;
    private final Map<Key, NamedBinding> sorted;
    private final Function<Key, ParameterSpec> names;

    private BuilderImpl(
            ComponentElement component,
            Map<Key, NamedBinding> sorted,
            Function<Key, ParameterSpec> names) {
        this.component = component;
        this.sorted = sorted;
        this.names = names;
    }

    TypeSpec generate(BuilderElement builder, MockBuilder mockBuilder) {
        if (component.omitMockBuilder()) {
            return generateNoMock(builder);
        } else {
            return generateMock(builder, mockBuilder);
        }
    }

    private TypeSpec generateMock(BuilderElement builder, MockBuilder mockBuilder) {
        TypeMirror builderType = builder.element().asType();
        TypeSpec.Builder spec = TypeSpec.classBuilder(builder.generatedClass());
        FieldSpec mockBuilderField = FieldSpec.builder(mockBuilder.getClassName(), "mockBuilder", FINAL).build();
        spec.addField(mockBuilderField);
        ParameterSpec mockBuilderParam = ParameterSpec.builder(mockBuilder.getClassName(), "mockBuilder").build();
        spec.addMethod(MethodSpec.constructorBuilder()
                .addParameter(mockBuilderParam)
                .addStatement("this.$N = $N", mockBuilderField, mockBuilderParam)
                .build());
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder(builder.buildMethod().getSimpleName().toString());
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            if (b instanceof ParameterBinding) {
                continue;
            }
            Key key = b.key();
            CodeBlock invocation = b.invocation(names);
            ParameterSpec param = names.apply(key);
            if (!key.typeName().isPrimitive()) {
                buildMethod.addStatement("$1T $2N = this.$3N != null && this.$3N.$2N != null ? this.$3N.$2N : $4L",
                        key.typeName(), param, mockBuilderField, invocation);
            } else {
                FieldSpec auxField = FieldSpec.builder(TypeName.BOOLEAN, namedBinding.auxName(), PRIVATE).build();
                buildMethod.addStatement("$1T $2N = this.$3N != null && this.$3N.$4N ? this.$3N.$2N : $5L",
                        key.typeName(), param, mockBuilderField, auxField, invocation);
            }
        }
        spec.addFields(fields());
        spec.addMethods(setterMethods(builder));
        spec.addModifiers(PRIVATE, STATIC, FINAL);
        spec.addSuperinterface(builderType);
        buildMethod.addAnnotation(Override.class);
        buildMethod.addModifiers(builder.buildMethod().getModifiers().stream()
                .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
        buildMethod.returns(TypeName.get(component.element().asType()));
        buildMethod.addStatement("return new $T($L)", component.generatedClass(), constructorParameters().stream()
                .collect(CodeBlock.joining(", ")));
        spec.addMethod(buildMethod.build());
        return spec.build();
    }

    private TypeSpec generateNoMock(BuilderElement builder) {
        TypeMirror builderType = builder.element().asType();
        TypeSpec.Builder spec = TypeSpec.classBuilder(builder.generatedClass());
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder(builder.buildMethod().getSimpleName().toString());
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            Key key = b.key();
            CodeBlock invocation = b.invocation(names);
            ParameterSpec param = names.apply(key);
            if (!(b instanceof ParameterBinding)) {
                buildMethod.addStatement("$T $N = $L", key.typeName(), param, invocation);
            }
        }
        spec.addFields(fields());
        spec.addMethods(setterMethods(builder));
        spec.addModifiers(PRIVATE, STATIC, FINAL);
        spec.addSuperinterface(builderType);
        buildMethod.addAnnotation(Override.class);
        buildMethod.addModifiers(builder.buildMethod().getModifiers().stream()
                .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
        buildMethod.returns(TypeName.get(component.element().asType()));
        buildMethod.addStatement("return new $T($L)", component.generatedClass(), constructorParameters().stream()
                .collect(CodeBlock.joining(", ")));
        spec.addMethod(buildMethod.build());
        return spec.build();
    }

    private List<FieldSpec> fields() {
        List<FieldSpec> result = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            if (b instanceof ParameterBinding) {
                result.add(FieldSpec.builder(b.key().typeName(), names.apply(b.key()).name).build());
            }
        }
        return result;
    }

    private List<MethodSpec> setterMethods(BuilderElement builder) {
        TypeMirror builderType = builder.element().asType();
        List<MethodSpec> result = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            if (b instanceof ParameterBinding) {
                MethodSpec.Builder setterMethod = MethodSpec.methodBuilder(b.element().getSimpleName().toString());
                setterMethod.addAnnotation(Override.class);
                setterMethod.addParameter(names.apply(b.key()));
                setterMethod.addStatement("this.$N = $N", names.apply(b.key()), names.apply(b.key()));
                setterMethod.addStatement("return this");
                setterMethod.returns(TypeName.get(builderType));
                setterMethod.addModifiers(b.element().getModifiers().stream()
                        .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
                result.add(setterMethod.build());
            }
        }
        return result;
    }

    private List<CodeBlock> constructorParameters() {
        List<CodeBlock> result = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            Key key = b.key();
            if (namedBinding.isComponentRequest()) {
                result.add(CodeBlock.of("$N", names.apply(key)));
            }
        }
        return result;
    }

    public static final class Factory {
        private final ComponentElement component;

        @Inject
        public Factory(ComponentElement component) {
            this.component = component;
        }

        BuilderImpl create(
                Map<Key, NamedBinding> sorted,
                Function<Key, ParameterSpec> names) {
            return new BuilderImpl(
                    component,
                    sorted,
                    names);
        }
    }
}
