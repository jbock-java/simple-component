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

import static io.jbock.simple.processor.util.Visitors.PACKAGE_VISITOR;
import static io.jbock.simple.processor.util.Visitors.TYPE_ELEMENT_VISITOR;

class Accessibility {

    private final Graph graph;
    private final PackageElement componentPackage;
    private final ComponentElement component;

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
        if (e.getModifiers().contains(Modifier.PRIVATE)) {
            throw new ValidationFailure(b.signature() + "The binding may not be private", e);
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
            throw new ValidationFailure("Non-static inner class is not allowed here", t);
        }
    }
}
