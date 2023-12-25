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
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public final class BuilderImpl {

    private final ComponentElement component;
    private final Map<Key, NamedBinding> sorted;
    private final Function<Key, ParameterSpec> names;

    @Inject
    public BuilderImpl(
            ComponentElement component,
            Context context) {
        this.component = component;
        this.sorted = context.sorted();
        this.names = context.names();
    }

    TypeSpec generate(BuilderElement builder, MockBuilder mockBuilder) {
        TypeMirror builderType = builder.element().asType();
        TypeSpec.Builder spec = TypeSpec.classBuilder(builder.generatedClass());
        spec.addFields(fields());
        spec.addMethods(setterMethods(builder));
        if (component.mockBuilder()) {
            spec.addMethod(generateWithMocksMethod(mockBuilder));
        }
        spec.addModifiers(PUBLIC, STATIC, FINAL);
        spec.addSuperinterface(builderType);
        spec.addMethod(generateBuildMethod(builder));
        return spec.build();
    }

    private MethodSpec generateBuildMethod(BuilderElement builder) {
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder(builder.buildMethod().getSimpleName().toString());
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            Key key = b.key();
            CodeBlock invocation = b.invocation(names, sorted, true);
            ParameterSpec param = names.apply(key);
            if (!(b instanceof ParameterBinding)) {
                buildMethod.addStatement("$T $N = $L", key.typeName(), param, invocation);
            }
        }
        buildMethod.addAnnotation(Override.class);
        buildMethod.addModifiers(builder.buildMethod().getModifiers().stream()
                .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
        buildMethod.returns(TypeName.get(component.element().asType()));
        buildMethod.addStatement("return new $T($L)", component.generatedClass(), constructorParameters().stream()
                .collect(CodeBlock.joining(", ")));
        return buildMethod.build();
    }

    private MethodSpec generateWithMocksMethod(MockBuilder mockBuilder) {
        MethodSpec.Builder method = MethodSpec.methodBuilder("withMocks");
        List<CodeBlock> constructorParameters = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            if (!(b instanceof ParameterBinding)) {
                continue;
            }
            ParameterSpec param = names.apply(b.key());
            constructorParameters.add(CodeBlock.of("this.$N", param));
        }
        if (component.publicMockBuilder()) {
            method.addModifiers(PUBLIC);
        }
        method.returns(mockBuilder.getClassName());
        method.addStatement("return new $T($L)", mockBuilder.getClassName(),
                constructorParameters.stream().collect(CodeBlock.joining(", ")));
        return method.build();
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
        List<MethodSpec> result = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Binding b = namedBinding.binding();
            if (!(b instanceof ParameterBinding)) {
                continue;
            }
            MethodSpec.Builder setterMethod = MethodSpec.methodBuilder(b.element().getSimpleName().toString());
            setterMethod.addAnnotation(Override.class);
            setterMethod.addParameter(names.apply(b.key()));
            setterMethod.addStatement("this.$1N = $1N", names.apply(b.key()));
            setterMethod.addStatement("return this");
            setterMethod.returns(builder.generatedClass());
            setterMethod.addModifiers(b.element().getModifiers().stream()
                    .filter(m -> m == PUBLIC || m == PROTECTED).collect(Collectors.toList()));
            result.add(setterMethod.build());
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
}
