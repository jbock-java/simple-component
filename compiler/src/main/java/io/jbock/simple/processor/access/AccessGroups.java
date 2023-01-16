package io.jbock.simple.processor.access;

import io.jbock.simple.processor.binding.InjectBinding;
import io.jbock.simple.processor.util.Visitors;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AccessGroups {

    private final Map<TypeElement, List<InjectBinding>> accessGroups;

    private AccessGroups(Map<TypeElement, List<InjectBinding>> accessGroups) {
        this.accessGroups = accessGroups;
    }

    public static AccessGroups create(Collection<InjectBinding> bindings) {
        return new AccessGroups(bindings.stream().collect(Collectors.groupingBy(
                b -> Visitors.TYPE_ELEMENT_VISITOR.visit(b.element().getEnclosingElement()),
                LinkedHashMap::new,
                Collectors.toList())));
    }

    public List<AccessGroup> groups() {
        List<AccessGroup> result = new ArrayList<>(accessGroups.size());
        for (Map.Entry<TypeElement, List<InjectBinding>> e : accessGroups.entrySet()) {
            TypeElement typeElement = e.getKey();
            List<InjectBinding> bindings = e.getValue();
            result.add(AccessGroup.create(typeElement, bindings));
        }
        return result;
    }
}
