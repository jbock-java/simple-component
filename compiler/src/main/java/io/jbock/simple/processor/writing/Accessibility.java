package io.jbock.simple.processor.writing;

import io.jbock.javapoet.TypeName;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.util.ComponentElement;
import io.jbock.simple.processor.util.ValidationFailure;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor14;

class Accessibility {

    private final Graph graph;
    private final PackageElement componentPackage;
    private final ComponentElement component;
    private static final SimpleElementVisitor14<PackageElement, Void> PACKAGE_VISITOR = new SimpleElementVisitor14<>() {
        @Override
        public PackageElement visitPackage(PackageElement e, Void unused) {
            return e;
        }
    };
    private static final SimpleElementVisitor14<TypeElement, Void> TYPE_ELEMENT_VISITOR = new SimpleElementVisitor14<>() {
        @Override
        public TypeElement visitType(TypeElement e, Void unused) {
            return e;
        }
    };

    private Accessibility(
            PackageElement componentPackage,
            Graph graph,
            ComponentElement component) {
        this.graph = graph;
        this.componentPackage = componentPackage;
        this.component = component;
    }

    static void check(ComponentElement component, Graph graph) {
        new Accessibility(getPackage(component.element()), graph, component).check();
    }

    private void check() {
        for (Binding binding : graph.nodes()) {
            if (binding instanceof InjectBinding b) {
                checkBinding(b);
            }
        }
    }

    private void checkBinding(InjectBinding b) {
        ExecutableElement e = b.element();
        if (getPackage(e).equals(componentPackage)) {
            return;
        }
        if (!e.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ValidationFailure(b.signature() + " is not accessible from "
                    + component.element().getQualifiedName(), e);
        }
        TypeElement nonPublic = findNonPublicEnclosing(e.getEnclosingElement());
        if (nonPublic == null) {
            return;
        }
        throw new ValidationFailure(TypeName.get(nonPublic.asType()) + " is not accessible from "
                + component.element().getQualifiedName(), e);
    }

    private static PackageElement getPackage(Element element) {
        Element current = element;
        while (current != null) {
            if (current.getModifiers().contains(Modifier.PRIVATE)) {
                throw new ValidationFailure("Enclosing type may not be private", current);
            }
            checkNesting(TYPE_ELEMENT_VISITOR.visit(current));
            PackageElement e = PACKAGE_VISITOR.visit(current);
            if (e != null) {
                return e;
            }
            current = current.getEnclosingElement();
        }
        throw new AssertionError("we should never get here");
    }

    private static TypeElement findNonPublicEnclosing(Element element) {
        Element current = element;
        while (current != null) {
            TypeElement t = TYPE_ELEMENT_VISITOR.visit(current);
            if (t == null) {
                return null;
            }
            if (!t.getModifiers().contains(Modifier.PUBLIC)) {
                return t;
            }
            current = current.getEnclosingElement();
        }
        return null;
    }

    private static void checkNesting(TypeElement t) {
        if (t == null) {
            return;
        }
        if (t.getNestingKind() == NestingKind.MEMBER && !t.getModifiers().contains(Modifier.STATIC)) {
            throw new ValidationFailure("Enclosing nested class must be static", t);
        }
    }
}
