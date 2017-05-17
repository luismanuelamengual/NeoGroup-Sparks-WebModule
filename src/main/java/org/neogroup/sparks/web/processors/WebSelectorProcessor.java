
package org.neogroup.sparks.web.processors;

import org.neogroup.sparks.processors.ProcessorComponent;
import org.neogroup.sparks.processors.SelectorProcessor;
import org.neogroup.sparks.web.commands.WebCommand;
import org.neogroup.sparks.web.routing.Route;

import java.util.HashMap;
import java.util.Map;

/**
 * Web processor selector
 */
@ProcessorComponent(commands = {WebCommand.class})
public class WebSelectorProcessor extends SelectorProcessor<WebCommand, WebProcessor> {

    private final Map<String, Class<? extends WebProcessor>> processorsByRoute;

    /**
     * Constructor for the web selector processor
     */
    public WebSelectorProcessor() {
        this.processorsByRoute = new HashMap<>();
    }

    /**
     * Registers a web processor class
     * @param webProcessorClass web processor class
     * @return boolean
     */
    @Override
    public boolean registerProcessorClass(Class<? extends WebProcessor> webProcessorClass) {
        boolean registered = false;
        Route webRoute = webProcessorClass.getAnnotation(Route.class);
        if (webRoute != null) {
            processorsByRoute.put(webRoute.path(), webProcessorClass);
            registered = true;
        }
        return registered;
    }

    /**
     * Get a web processor class from a command
     * @param command command
     * @return web processor class
     */
    @Override
    protected Class<? extends WebProcessor> getProcessorClass(WebCommand command) {
        return processorsByRoute.get(command.getWebRoute());
    }
}
