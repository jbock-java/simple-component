package io.jbock.simple.processor.util;

import io.jbock.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.DeclaredType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SimpleAnnotation {

    private final DeclaredType annotationType;
    private final TypeName typeName;
    private final List<AnnotationValue> values;
    private final SafeTypes types;

    private SimpleAnnotation(
            DeclaredType annotationType,
            List<AnnotationValue> values,
            SafeTypes types) {
        this.annotationType = annotationType;
        this.values = values;
        this.types = types;
        this.typeName = TypeName.get(annotationType);
    }

    public static SimpleAnnotation create(
            AnnotationMirror mirror,
            SafeElements elements,
            SafeTypes types) {
        DeclaredType annotationType = mirror.getAnnotationType();
        List<AnnotationValue> values = new ArrayList<>(elements.getElementValuesWithDefaults(mirror).values());
        return new SimpleAnnotation(annotationType, values, types);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleAnnotation that = (SimpleAnnotation) o;
        if (!types.isSameType(annotationType, that.annotationType)) {
            return false;
        }
        if (values.size() != that.values.size()) {
            return false;
        }
        for (int i = 0; i < values.size(); i++) {
            if (!Objects.equals(values.get(i).getValue(), that.values.get(i).getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int[] result = new int[values.size() + 1];
        result[0] = Objects.hashCode(typeName); //avoid TypeMirror hashCode
        for (int i = 0; i < values.size(); i++) {
            result[i + 1] = Objects.hashCode(values.get(i).getValue());
        }
        return Arrays.hashCode(result);
    }

    @Override
    public String toString() {
        String simpleName = Visitors.TYPE_ELEMENT_VISITOR.visit(annotationType.asElement()).getSimpleName().toString();
        if (values.isEmpty()) {
            return simpleName;
        }
        return simpleName + values.stream().map(Objects::toString).collect(Collectors.joining(", ", "(", ")"));
    }
}
