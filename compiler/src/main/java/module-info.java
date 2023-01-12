import io.jbock.simple.processor.SimpleComponentProcessor;

module io.jbock.simple.compiler {

    provides javax.annotation.processing.Processor with SimpleComponentProcessor;

    requires jakarta.inject;
    requires java.compiler;
    requires io.jbock.auto.common;
    requires io.jbock.javapoet;
    requires io.jbock.simple;
}