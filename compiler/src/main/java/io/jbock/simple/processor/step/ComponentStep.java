package io.jbock.simple.processor.step;

import io.jbock.auto.common.BasicAnnotationProcessor.Step;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import io.jbock.simple.processor.ContextComponent;
import io.jbock.simple.processor.binding.Binding;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.util.SpecWriter;
import io.jbock.simple.processor.util.TypeTool;
import io.jbock.simple.processor.util.ValidationFailure;
import io.jbock.simple.processor.validation.ExecutableElementValidator;
import io.jbock.simple.processor.validation.TypeElementValidator;
import io.jbock.simple.processor.writing.Generator;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentStep implements Step {

    private final Messager messager;
    private final TypeTool tool;
    private final TypeElementValidator typeElementValidator;
    private final ExecutableElementValidator executableElementValidator;
    private final SpecWriter specWriter;
    private final ComponentElement.Factory componentElementFactory;
    private final ContextComponent.Factory contextComponentFactory;

    @Inject
    public ComponentStep(
            Messager messager,
            TypeTool tool,
            TypeElementValidator typeElementValidator,
            ExecutableElementValidator executableElementValidator,
            SpecWriter specWriter,
            ComponentElement.Factory componentElementFactory,
            ContextComponent.Factory contextComponentFactory) {
        this.messager = messager;
        this.tool = tool;
        this.typeElementValidator = typeElementValidator;
        this.executableElementValidator = executableElementValidator;
        this.specWriter = specWriter;
        this.componentElementFactory = componentElementFactory;
        this.contextComponentFactory = contextComponentFactory;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(Component.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        List<Element> elements = elementsByAnnotation.values().stream().flatMap(Set::stream).collect(Collectors.toList());
        List<TypeElement> typeElements = ElementFilter.typesIn(elements);
        for (TypeElement typeElement : typeElements) {
            try {
                process(typeElement);
            } catch (ValidationFailure f) {
                f.writeTo(messager);
            }
        }
        return Set.of();
    }

    private void process(TypeElement typeElement) {
        typeElementValidator.validate(typeElement);
        ComponentElement component = componentElementFactory.create(typeElement);
        component.factoryElement().ifPresent(factory -> {
            ExecutableElement method = factory.singleAbstractMethod();
            if (!tool.types().isSameType(method.getReturnType(), typeElement.asType())) {
                throw new ValidationFailure("Factory method must return the component type", method);
            }
        });
        for (ExecutableElement m : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (m.getModifiers().contains(Modifier.ABSTRACT)) {
                executableElementValidator.validate(m);
            }
        }
        ContextComponent componentComponent = contextComponentFactory.create(component);
        Generator generator = componentComponent.generator();
        List<Binding> bindings = componentComponent.topologicalSorter().sortedBindings();
        TypeSpec typeSpec = generator.generate(bindings);
        specWriter.write(component.generatedClass(), typeSpec);
    }
}
