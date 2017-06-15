
package org.neogroup.sparks.web.routing;

import org.neogroup.sparks.web.processors.WebProcessor;

import java.lang.reflect.Method;

public class RouteEntry {

    private final String httpMethod;
    private final String path;
    private final WebProcessor processor;
    private final Method processorMethod;

    public RouteEntry(String httpMethod, String path, WebProcessor processor, Method processorMethod) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.processor = processor;
        this.processorMethod = processorMethod;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public WebProcessor getProcessor() {
        return processor;
    }

    public Method getProcessorMethod() {
        return processorMethod;
    }
}
