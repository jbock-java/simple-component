package io.jbock.simple.processor.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ParameterBinding;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class MockBuilder {

    private final ComponentElement component;
    private final Map<Key, NamedBinding> sorted;
    private final Function<Key, ParameterSpec> names;
    private final Modifier[] modifiers;

    MockBuilder(
            ComponentElement component,
            Map<Key, NamedBinding> sorted,
            Function<Key, ParameterSpec> names) {
        this.component = component;
        this.sorted = sorted;
        this.names = names;
        this.modifiers = component.element().getModifiers().stream()
                .filter(m -> m == PUBLIC).toArray(Modifier[]::new);
    }

    TypeSpec generate() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(getClassName())
                .addModifiers(modifiers)
                .addModifiers(STATIC, FINAL);
        spec.addMethod(buildMethod());
        spec.addFields(getFields());
        spec.addMethods(getMethods());
        spec.addMethod(generateConstructor());
        return spec.build();
    }

    private MethodSpec generateConstructor() {
        return MethodSpec.constructorBuilder().addModifiers(PRIVATE).build();
    }

    ClassName getClassName() {
        return component.generatedClass().nestedClass("MockBuilder");
    }

    private MethodSpec buildMethod() {
        List<CodeBlock> constructorParameters = new ArrayList<>();
        MethodSpec.Builder method = MethodSpec.methodBuilder("build");
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
            } else if (!key.typeName().isPrimitive()) {
                method.addStatement("$1T $2N = this.$2N != null ? this.$2N : $3L", key.typeName(), param, invocation);
            } else {
                FieldSpec auxField = FieldSpec.builder(TypeName.BOOLEAN, namedBinding.auxName(), PRIVATE).build();
                method.addStatement("$1T $2N = this.$3N ? this.$2N : $4L", key.typeName(), param, auxField, invocation);
            }
        }
        return method
                .addModifiers(modifiers)
                .returns(TypeName.get(component.element().asType()))
                .addStatement("return new $T($L)",
                        component.generatedClass(),
                        constructorParameters.stream().collect(CodeBlock.joining(", ")))
                .build();
    }

    private List<FieldSpec> getFields() {
        List<FieldSpec> fields = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            if (namedBinding.binding() instanceof ParameterBinding) {
                continue;
            }
            TypeName type = namedBinding.binding().key().typeName();
            FieldSpec field = FieldSpec.builder(type, namedBinding.name(), PRIVATE).build();
            fields.add(field);
            if (namedBinding.binding().key().typeName().isPrimitive()) {
                FieldSpec auxField = FieldSpec.builder(TypeName.BOOLEAN, namedBinding.auxName(), PRIVATE).build();
                fields.add(auxField);
            }
        }
        return fields;
    }

    private List<MethodSpec> getMethods() {
        List<MethodSpec> methods = new ArrayList<>();
        for (NamedBinding namedBinding : sorted.values()) {
            if (namedBinding.binding() instanceof ParameterBinding) {
                continue;
            }
            Binding b = namedBinding.binding();
            Key key = b.key();
            ParameterSpec param = names.apply(key);
            MethodSpec.Builder method = MethodSpec.methodBuilder(param.name)
                    .addModifiers(modifiers)
                    .addParameter(param)
                    .addStatement("this.$1N = $1N", param);
            if (namedBinding.binding().key().typeName().isPrimitive()) {
                FieldSpec auxField = FieldSpec.builder(TypeName.BOOLEAN, namedBinding.auxName(), PRIVATE).build();
                method.addStatement("this.$N = $L", auxField, true);
            }
            methods.add(method.build());
        }
        return methods;
    }

    public static final class Factory {
        private final ComponentElement component;

        @Inject
        public Factory(ComponentElement component) {
            this.component = component;
        }

        MockBuilder create(
                Map<Key, NamedBinding> sorted,
                Function<Key, ParameterSpec> names) {
            return new MockBuilder(component, sorted, names);
        }
    }
}
