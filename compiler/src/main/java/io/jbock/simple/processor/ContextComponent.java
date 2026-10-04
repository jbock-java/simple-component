package io.jbock.simple.processor;

import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import io.jbock.simple.Provides;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.Key;
import io.jbock.simple.processor.binding.KeyFactory;
import io.jbock.simple.processor.binding.Node;
import io.jbock.simple.processor.graph.TopologicalSorter;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.UniqueNameSet;
import io.jbock.simple.processor.writing.ComponentImpl;
import io.jbock.simple.processor.writing.Context;
import io.jbock.simple.processor.writing.NamedBinding;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public interface ContextComponent {

    @Component.Builder
    interface Builder {
        Builder typeElement(TypeElement typeElement);

        Builder tool(TypeTool tool);

        ContextComponent build();
    }

    KeyFactory keyFactory();

    ComponentElement componentElement();

    ComponentImpl componentImpl();

    @Provides
    static Context createContext(
            TopologicalSorter topologicalSorter,
            KeyFactory keyFactory) {
        List<Node> bindings = topologicalSorter.sortedBindings();
        Map<Key, NamedBinding> sorted = addNames(keyFactory, bindings);
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
        return switch (candidateName) {
            case "package" -> "pkg";
            case "boolean", "byte" -> "b";
            case "double" -> "d";
            case "int" -> "i";
            case "short" -> "s";
            case "char" -> "c";
            case "void" -> "v";
            case "class" -> "clazz";
            case "float" -> "f";
            case "long" -> "l";
            default -> SourceVersion.isKeyword(candidateName) ? candidateName + '_' : candidateName;
        };
    }

    final class Factory {
        private final TypeTool tool;

        @Inject
        public Factory(
                TypeTool tool) {
            this.tool = tool;
        }

        public ContextComponent create(TypeElement typeElement) {
            return ContextComponent_Impl.builder()
                    .typeElement(typeElement)
                    .tool(tool)
                    .build();
        }
    }
}
