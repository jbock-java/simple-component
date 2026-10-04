package io.jbock.simple.processor.writing;

import io.jbock.simple.Modulus;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.binding.Node;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.KeyFactory;
import io.jbock.simple.processor.graph.TopologicalSorter;
import io.jbock.simple.processor.util.UniqueNameSet;

import javax.lang.model.SourceVersion;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Modulus
public interface ContextModule {

    @Provides
    static Context createContext(
            TopologicalSorter topologicalSorter,
            KeyFactory keyFactory) {
        List<Node> bindings = topologicalSorter.sortedBindings();
        Map<Key, NamedBinding> sorted = ContextModule.addNames(keyFactory, bindings);
        return new Context(sorted);
    }

    private static Map<Key, NamedBinding> addNames(
            KeyFactory keyFactory,
            List<Node> bindings) {
        UniqueNameSet uniqueNameSet = new UniqueNameSet();
        uniqueNameSet.claim("mockBuilder");
        uniqueNameSet.claim("withMocks");
        uniqueNameSet.claim("build");
        Map<Key, NamedBinding> result = new LinkedHashMap<>();
        for (Node b : bindings) {
            String name = uniqueNameSet.getUniqueName(validJavaName(b.suggestedVariableName()));
            String auxName = uniqueNameSet.getUniqueName(name + "_isSet");
            result.put(b.key(), new NamedBinding(b, name, auxName, keyFactory.isComponentRequest(b)));
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
