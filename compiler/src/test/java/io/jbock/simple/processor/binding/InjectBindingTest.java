package io.jbock.simple.processor.binding;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InjectBindingTest {

    @Test
    void testSimpleTypeName() {
        ClassName m = ClassName.get(Map.class);
        ClassName s = ClassName.get(String.class);
        ClassName f = ClassName.get(Function.class);
        ClassName i = ClassName.get(Integer.class);
        ParameterizedTypeName type = ParameterizedTypeName.get(m, s, ParameterizedTypeName.get(f, s, i));
        Assertions.assertEquals("MapStringFunctionStringInteger", InjectBinding.simpleTypeName(type));
    }
}
