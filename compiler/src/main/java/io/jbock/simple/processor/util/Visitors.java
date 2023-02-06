package io.jbock.simple.processor.util;

import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleElementVisitor9;
import javax.lang.model.util.SimpleTypeVisitor9;

public final class Visitors {

    public static final ElementVisitor<PackageElement, Void> PACKAGE_VISITOR = new SimpleElementVisitor9<>() {
        @Override
        public PackageElement visitPackage(PackageElement e, Void unused) {
            return e;
        }
    };

    public static final ElementVisitor<TypeElement, Void> TYPE_ELEMENT_VISITOR = new SimpleElementVisitor9<>() {
        @Override
        public TypeElement visitType(TypeElement e, Void unused) {
            return e;
        }
    };

    public static final ElementVisitor<ExecutableElement, Void> EXECUTABLE_ELEMENT_VISITOR = new SimpleElementVisitor9<>() {

        @Override
        public ExecutableElement visitExecutable(ExecutableElement e, Void unused) {
            return e;
        }
    };

    public static final TypeVisitor<DeclaredType, Void> DECLARED_TYPE_VISITOR = new SimpleTypeVisitor9<>() {
        @Override
        public DeclaredType visitDeclared(DeclaredType declaredType, Void unused) {
            return declaredType;
        }
    };


    private Visitors() {
    }
}
