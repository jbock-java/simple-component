package io.jbock.simple.processor.graph;

import io.jbock.simple.Inject;
import io.jbock.simple.processor.binding.ComponentElement;
import io.jbock.simple.processor.binding.DependencyRequest;
import io.jbock.simple.processor.util.ValidationFailure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.jbock.simple.processor.util.Printing.DOUBLE_INDENT;
import static io.jbock.simple.processor.util.Printing.INDENT;
import static io.jbock.simple.processor.util.Printing.bindingElementToString;

public final class MissingBindingPrinter {

    private final ComponentElement component;

    @Inject
    public MissingBindingPrinter(ComponentElement component) {
        this.component = component;
    }

    ValidationFailure fail(List<DependencyRequest> dependencyTrace) {
        List<DependencyRequest> trace = new ArrayList<>(dependencyTrace);
        Collections.reverse(trace);
        return failInternal(trace);
    }

    private ValidationFailure failInternal(List<DependencyRequest> trace) {
        DependencyRequest request = trace.get(0);
        StringBuilder message = new StringBuilder();
        message.append("No binding found for " + request.key().toString() + ".");
        for (int i = 0; i < trace.size(); i++) {
            DependencyRequest r = trace.get(i);
            String formatted = format(r, i == trace.size() - 1 ? "requested" : "injected");
            message.append("\n").append(formatted);
        }
        return new ValidationFailure(message.toString(), component.element());
    }

    private String format(DependencyRequest request, String verb) {
        return INDENT
                + request.key()
                + " is "
                + verb
                + " at\n"
                + DOUBLE_INDENT
                + bindingElementToString(request.requestElement());
    }
}
