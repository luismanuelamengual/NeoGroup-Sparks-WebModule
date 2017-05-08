
package org.neogroup.sparks.web.processors;

import org.neogroup.sparks.processors.ProcessorComponent;
import org.neogroup.sparks.processors.SelectorProcessor;
import org.neogroup.sparks.web.commands.WebCommand;
import org.neogroup.sparks.web.routing.Route;

import java.util.HashMap;
import java.util.Map;

@ProcessorComponent(commands = {WebCommand.class})
public class WebSelectorProcessor extends SelectorProcessor<WebCommand, WebProcessor> {

    private final Map<String, WebProcessor> processorsByRoute;

    public WebSelectorProcessor() {
        this.processorsByRoute = new HashMap<>();
    }

    @Override
    public boolean registerProcessorCandidate(WebProcessor processor) {
        boolean registered = false;
        Route webRoute = processor.getClass().getAnnotation(Route.class);
        if (webRoute != null) {
            processorsByRoute.put(webRoute.path(), processor);
            registered = true;
        }
        return registered;
    }

    @Override
    public WebProcessor getProcessor(WebCommand command) {
        return processorsByRoute.get(command.getWebRoute());
    }
}
