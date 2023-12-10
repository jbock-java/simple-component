package io.jbock.simple.processor.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.FactoryElement;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;

import javax.lang.model.element.ExecutableElement;
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

public class FactoryImpl {

    private final ComponentElement component;
    private final Map<Key, NamedBinding> sorted;
    private final Function<Key, ParameterSpec> names;

    private FactoryImpl(
            ComponentElement component,
            Map<Key, NamedBinding> sorted,
            Function<Key, ParameterSpec> names) {
        this.component = component;
        this.sorted = sorted;
        this.names = names;
    }

    TypeSpec generate(FactoryElement factory, MockBuilder mockBuilder, MockBuilder2 mockBuilder2) {
        if (component.mockBuilder()) {
            return generateMock(factory, mockBuilder, mockBuilder2);
        } else {
            return generateNoMock(factory);
        }
    }

    private TypeSpec generateMock(FactoryElement factory, MockBuilder mockBuilder, MockBuilder2 mockBuilder2) {
        TypeSpec.Builder spec = TypeSpec.classBuilder(factory.generatedClass());
        FieldSpec mockBuilderField = FieldSpec.builder(mockBuilder.getClassName(), "mockBuilder", FINAL).build();
        spec.addField(mockBuilderField);
        ParameterSpec mockBuilderParam = ParameterSpec.builder(mockBuilder.getClassName(), "mockBuilder").build();
        spec.addMethod(MethodSpec.constructorBuilder()
                .addParameter(mockBuilderParam)
                .addStatement("this.$N = $N", mockBuilderField, mockBuilderParam)
                .build());
        ExecutableElement abstractMethod = factory.singleAbstractMethod();
        spec.addMethod(generateBuildMethod(abstractMethod, mockBuilderField));
        spec.addMethod(generateWithMocksMethod(mockBuilder2));
        spec.addModifiers(PUBLIC, STATIC, FINAL);
        spec.addSuperinterface(factory.element().asType());
        return spec.build();
    }

    private MethodSpec generateBuildMethod(ExecutableElement abstractMethod, FieldSpec mockBuilderField) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(abstractMethod.getSimpleName().toString());
        method.addAnnotation(Override.class);
        method.addModifiers(abstractMethod.getModifiers().stream()
                .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
        method.returns(TypeName.get(component.element().asType()));
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            if (b instanceof ParameterBinding) {
                continue;
            }
            Key key = b.key();
            CodeBlock invocation = b.invocation(names);
            ParameterSpec param = names.apply(key);
            if (!key.typeName().isPrimitive()) {
                method.addStatement("$1T $2N = this.$3N != null && this.$3N.$2N != null ? this.$3N.$2N : $4L",
                        key.typeName(), param, mockBuilderField, invocation);
            } else {
                FieldSpec auxField = FieldSpec.builder(TypeName.BOOLEAN, namedBinding.auxName(), PRIVATE).build();
                method.addStatement("$1T $2N = this.$3N != null && this.$3N.$4N ? this.$3N.$2N : $5L",
                        key.typeName(), param, mockBuilderField, auxField, invocation);
            }
        }
        method.addParameters(parameters());
        method.addStatement("return new $T($L)", component.generatedClass(), constructorParameters().stream()
                .collect(CodeBlock.joining(", ")));
        return method.build();
    }

    private MethodSpec generateWithMocksMethod(MockBuilder2 mockBuilder2) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("withMocks");
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
        method.addParameters(parameters());
        method.returns(mockBuilder2.getClassName());
        method.addStatement("return new $T($L)", mockBuilder2.getClassName(),
                constructorParameters.stream().collect(CodeBlock.joining(", ")));
        return method.build();
    }

    private TypeSpec generateNoMock(FactoryElement factory) {
        TypeSpec.Builder spec = TypeSpec.classBuilder(factory.generatedClass());
        spec.addModifiers(PRIVATE, STATIC, FINAL);
        spec.addSuperinterface(factory.element().asType());
        ExecutableElement abstractMethod = factory.singleAbstractMethod();
        MethodSpec.Builder method = MethodSpec.methodBuilder(abstractMethod.getSimpleName().toString());
        method.addAnnotation(Override.class);
        method.addModifiers(abstractMethod.getModifiers().stream()
                .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
        method.returns(TypeName.get(component.element().asType()));
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            Key key = b.key();
            CodeBlock invocation = b.invocation(names);
            ParameterSpec param = names.apply(key);
            if (!(b instanceof ParameterBinding)) {
                method.addStatement("$T $N = $L", key.typeName(), param, invocation);
            }
        }
        method.addParameters(parameters());
        method.addStatement("return new $T($L)", component.generatedClass(), constructorParameters().stream()
                .collect(CodeBlock.joining(", ")));
        spec.addMethod(method.build());
        return spec.build();
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

    private List<ParameterSpec> parameters() {
        List<ParameterSpec> result = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            Key key = b.key();
            if (b instanceof ParameterBinding) {
                result.add(names.apply(key));
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

        FactoryImpl create(
                Map<Key, NamedBinding> sorted,
                Function<Key, ParameterSpec> names) {
            return new FactoryImpl(
                    component,
                    sorted,
                    names);
        }
    }
}
