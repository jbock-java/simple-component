package io.jbock.simple.processor.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.Inject;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public final class AccessImpl {

    public static final class Spec {
        private final ClassName generatedClass;
        private final TypeSpec typeSpec;

        public Spec(ClassName generatedClass, TypeSpec typeSpec) {
            this.generatedClass = generatedClass;
            this.typeSpec = typeSpec;
        }

        public ClassName generatedClass() {
            return generatedClass;
        }

        public TypeSpec typeSpec() {
            return typeSpec;
        }
    }

    @Inject
    public AccessImpl() {
    }

    public Spec generate(
            TypeElement typeElement) {
        ClassName className = ClassName.get(typeElement);
        ClassName generatedClass = className
                .topLevelClassName()
                .peerClass(String.join("_", className.simpleNames()) + "_Access");
        TypeSpec.Builder spec = TypeSpec.classBuilder(generatedClass)
                .addModifiers(Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                .addAnnotation(ComponentImpl.generatedAnnotation())
                .addOriginatingElement(typeElement);
        return new Spec(generatedClass, spec.build());
    }
}
