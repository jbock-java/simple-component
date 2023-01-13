package io.jbock.simple.processor.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.util.ComponentElement;

import java.util.Map;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

public class ComponentImpl {

    TypeSpec generate(
            ComponentElement component,
            Map<Key, NamedBinding> sorted) {
        TypeSpec.Builder spec = TypeSpec.classBuilder(component.generatedClass()).addSuperinterface(component.element().asType());
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(PRIVATE);
        for (Map.Entry<Key, NamedBinding> e : sorted.entrySet()) {
            InjectBinding b = e.getValue().binding();
            Key key = b.key();
            String uniqueName = e.getValue().name();
            FieldSpec field = FieldSpec.builder(
                    key.typeName(), uniqueName, PRIVATE, FINAL).build();
            spec.addField(field);
            constructor.addStatement("this.$N = $L", field,
                    b.invokeExpression(b.dependencies().stream()
                            .map(d -> CodeBlock.of("$L", sorted.get(d.key()).name()))
                            .collect(CodeBlock.joining(", "))));
        }
        for (DependencyRequest r : component.getRequests()) {
            MethodSpec.Builder method = MethodSpec.methodBuilder(r.requestElement().getSimpleName().toString());
            method.addStatement("return $L", sorted.get(r.key()).name());
            method.returns(r.key().typeName());
            method.addAnnotation(Override.class);
            if (r.requestElement().getModifiers().contains(PUBLIC)) {
                method.addModifiers(PUBLIC);
            }
            if (r.requestElement().getModifiers().contains(PROTECTED)) {
                method.addModifiers(PROTECTED);
            }
            spec.addMethod(method.build());
        }
        return spec.addMethod(constructor.build())
                .addOriginatingElement(component.element()).build();
    }
}
