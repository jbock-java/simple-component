import io.jbock.simple.processor.SimpleComponentProcessor;

module io.jbock.simple.compiler {

    provides javax.annotation.processing.Processor with SimpleComponentProcessor;

    requires io.jbock.auto.common;
    requires com.palantir.javapoet;
    requires io.jbock.simple;
}
