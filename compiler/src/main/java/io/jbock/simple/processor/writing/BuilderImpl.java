package io.jbock.simple.processor.writing;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.Node;
import io.jbock.simple.processor.binding.BuilderElement;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public final class BuilderImpl {

    private final ComponentElement component;
    private final Map<Key, NamedBinding> sorted;

    @Inject
    public BuilderImpl(
            ComponentElement component,
            Context context) {
        this.component = component;
        this.sorted = context.sorted();
    }

    TypeSpec generate(BuilderElement builder) {
        TypeMirror builderType = builder.element().asType();
        TypeSpec.Builder spec = TypeSpec.classBuilder(builder.generatedClass());
        spec.addFields(fields());
        spec.addMethods(setterMethods(builder));
        spec.addModifiers(PUBLIC, STATIC, FINAL);
        spec.addSuperinterface(builderType);
        spec.addMethod(generateBuildMethod(builder));
        return spec.build();
    }

    private MethodSpec generateBuildMethod(BuilderElement builder) {
        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder(builder.buildMethod().getSimpleName().toString());
        for (NamedBinding namedBinding : sorted.values()) {
            Node b = namedBinding.binding();
            Key key = b.key();
            CodeBlock invocation = b.invocation(sorted, true);
            ParameterSpec param = namedBinding.parameter();
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

    private List<FieldSpec> fields() {
        List<FieldSpec> result = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Node b = namedBinding.binding();
            if (b instanceof ParameterBinding) {
                result.add(FieldSpec.builder(b.key().typeName(), namedBinding.parameter().name()).build());
            }
        }
        return result;
    }

    private List<MethodSpec> setterMethods(BuilderElement builder) {
        List<MethodSpec> result = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            Node b = namedBinding.binding();
            if (!(b instanceof ParameterBinding)) {
                continue;
            }
            MethodSpec.Builder setterMethod = MethodSpec.methodBuilder(b.element().getSimpleName().toString());
            setterMethod.addAnnotation(Override.class);
            setterMethod.addParameter(namedBinding.parameter());
            setterMethod.addStatement("this.$1N = $1N", namedBinding.parameter());
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
            Node b = namedBinding.binding();
            if (namedBinding.isComponentRequest()) {
                result.add(CodeBlock.of("$N", namedBinding.parameter()));
            }
        }
        return result;
    }
}
