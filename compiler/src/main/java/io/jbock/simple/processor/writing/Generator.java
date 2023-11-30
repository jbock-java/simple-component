package io.jbock.simple.processor.writing;

import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.util.UniqueNameSet;

import javax.lang.model.SourceVersion;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Generator {

    private final ComponentImpl.Factory componentImpl;
    private final ComponentElement component;

    @Inject
    public Generator(
            ComponentImpl.Factory componentImpl,
            ComponentElement component) {
        this.componentImpl = componentImpl;
        this.component = component;
    }

    public TypeSpec generate(List<Binding> bindings) {
        Map<Key, NamedBinding> sorted = addNames(bindings);
        return componentImpl.create(sorted, createNames(sorted)).generate();
    }

    private Map<Key, NamedBinding> addNames(List<Binding> bindings) {
        UniqueNameSet uniqueNameSet = new UniqueNameSet();
        uniqueNameSet.claim("mockBuilder");
        uniqueNameSet.claim("build");
        Map<Key, NamedBinding> result = new LinkedHashMap<>();
        for (Binding b : bindings) {
            String name = uniqueNameSet.getUniqueName(validJavaName(b.suggestedVariableName()));
            String auxName = uniqueNameSet.getUniqueName(name + "_isSet");
            result.put(b.key(), new NamedBinding(b, name, auxName, component.isComponentRequest(b)));
        }
        return result;
    }

    private static Function<Key, ParameterSpec> createNames(
            Map<Key, NamedBinding> sorted) {
        Map<Key, ParameterSpec> cache = new HashMap<>();
        return key -> {
            return cache.computeIfAbsent(key, k -> {
                String name = sorted.get(k).name();
                return ParameterSpec.builder(k.typeName(), name).build();
            });
        };
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
