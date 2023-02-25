package io.jbock.simple.processor.graph;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.util.ValidationFailure;

// MissingBindingValidator.reportMissingBinding
// DiagnosticMessageGenerator.getMessage(MissingBinding binding)
// DependencyRequestFormatter.format
public class MissingBindingPrinter {

    private final ComponentElement component;
    private final DependencyRequest request;

    private MissingBindingPrinter(ComponentElement component, DependencyRequest request) {
        this.component = component;
        this.request = request;
    }

    private ValidationFailure fail() {
        return new ValidationFailure(request.key().toString() + " cannot be provided without an @Inject constructor or a\n" +
                "                    + \"@Provides-annotated method.", component.element());
    }

    public static final class Factory {
        private final ComponentElement component;

        @Inject
        public Factory(ComponentElement component) {
            this.component = component;
        }

        ValidationFailure fail(DependencyRequest request) {
            return new MissingBindingPrinter(component, request).fail();
        }
    }
}
