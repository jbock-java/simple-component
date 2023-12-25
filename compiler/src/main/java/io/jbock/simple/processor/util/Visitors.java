package io.jbock.simple.processor.util;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import javax.lang.model.util.SimpleElementVisitor9;
import javax.lang.model.util.SimpleTypeVisitor9;
import java.util.List;

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

    public static final ElementVisitor<VariableElement, Void> PARAMETER_VISITOR = new SimpleElementVisitor9<>() {

        @Override
        public VariableElement visitVariable(VariableElement e, Void unused) {
            return e;
        }
    };

    public static final TypeVisitor<DeclaredType, Void> DECLARED_TYPE_VISITOR = new SimpleTypeVisitor9<>() {
        @Override
        public DeclaredType visitDeclared(DeclaredType declaredType, Void unused) {
            return declaredType;
        }
    };

    public static final AnnotationValueVisitor<TypeMirror, Void> ANNOTATION_VALUE_AS_TYPE = new SimpleAnnotationValueVisitor9<>() {

        @Override
        public TypeMirror visitType(TypeMirror mirror, Void unused) {
            return mirror;
        }
    };

    public static final AnnotationValueVisitor<List<? extends AnnotationValue>, Void> ANNOTATION_VALUE_AS_ARRAY = new SimpleAnnotationValueVisitor9<>() {

        @Override
        public List<? extends AnnotationValue> visitArray(List<? extends AnnotationValue> array, Void unused) {
            return array;
        }

        @Override
        protected List<? extends AnnotationValue> defaultAction(Object o, Void unused) {
            return List.of();
        }
    };

    private Visitors() {
    }
}
