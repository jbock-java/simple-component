package io.jbock.simple.processor.writing;

import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.util.UniqueNameSet;

import javax.lang.model.SourceVersion;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Generator {

    private final ComponentImpl componentImpl;
    private final ComponentElement component;

    public Generator(
            ComponentImpl componentImpl,
            ComponentElement component) {
        this.componentImpl = componentImpl;
        this.component = component;
    }

    public TypeSpec generate(Set<Binding> sorted) {
        return componentImpl.generate(component, addNames(sorted));
    }

    Map<Key, NamedBinding> addNames(Set<Binding> sorted) {
        UniqueNameSet uniqueNameSet = new UniqueNameSet();
        Map<Key, NamedBinding> result = new LinkedHashMap<>();
        for (Binding b : sorted) {
            String name = uniqueNameSet.getUniqueName(validJavaName(b.suggestedVariableName()));
            result.put(b.key(), new NamedBinding(b, name, component.isComponentRequest(b)));
        }
        return result;
    }

    private static String validJavaName(String name) {
        if (SourceVersion.isIdentifier(name)) {
            return protectAgainstKeywords(name);
        }
        StringBuilder newName = new StringBuilder(name.length());
        char firstChar = name.charAt(0);
        if (!Character.isJavaIdentifierStart(firstChar)) {
            newName.append('_');
        }
        name.chars().forEach(c -> newName.append(Character.isJavaIdentifierPart(c) ? c : '_'));
        return newName.toString();
    }

    private static String protectAgainstKeywords(String candidateName) {
        switch (candidateName) {
            case "package":
                return "pkg";
            case "boolean":
            case "byte":
                return "b";
            case "double":
                return "d";
            case "int":
                return "i";
            case "short":
                return "s";
            case "char":
                return "c";
            case "void":
                return "v";
            case "class":
                return "clazz";
            case "float":
                return "f";
            case "long":
                return "l";
            default:
                return SourceVersion.isKeyword(candidateName) ? candidateName + '_' : candidateName;
        }
    }
}
