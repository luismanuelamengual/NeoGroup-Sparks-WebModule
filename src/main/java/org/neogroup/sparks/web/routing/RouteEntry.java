
package org.neogroup.sparks.web.routing;

import org.neogroup.httpserver.HttpMethod;
import org.neogroup.sparks.web.processors.WebProcessor;

import java.lang.reflect.Method;

public class RouteEntry {

    private final HttpMethod httpMethod;
    private final String path;
    private final WebProcessor processor;
    private final Method processorMethod;

    public RouteEntry(HttpMethod httpMethod, String path, WebProcessor processor, Method processorMethod) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.processor = processor;
        this.processorMethod = processorMethod;
    }

    public HttpMethod getHttpMethod() {
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
