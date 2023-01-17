package io.jbock.simple.processor.util;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor14;

public final class Visitors {

    public static final SimpleElementVisitor14<PackageElement, Void> PACKAGE_VISITOR = new SimpleElementVisitor14<>() {
        @Override
        public PackageElement visitPackage(PackageElement e, Void unused) {
            return e;
        }
    };

    public static final SimpleElementVisitor14<TypeElement, Void> TYPE_ELEMENT_VISITOR = new SimpleElementVisitor14<>() {
        @Override
        public TypeElement visitType(TypeElement e, Void unused) {
            return e;
        }
    };

    public static final SimpleElementVisitor14<ExecutableElement, Void> EXECUTABLE_ELEMENT_VISITOR = new SimpleElementVisitor14<>() {

        @Override
        public ExecutableElement visitExecutable(ExecutableElement e, Void unused) {
            return e;
        }
    };

    private Visitors() {
    }
}
