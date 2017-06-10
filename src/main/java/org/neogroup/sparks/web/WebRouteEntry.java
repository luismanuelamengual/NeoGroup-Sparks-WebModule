
package org.neogroup.sparks.web;

import org.neogroup.sparks.web.processors.WebProcessor;

import java.lang.reflect.Method;

public class WebRouteEntry {

    private final String httpMethod;
    private final String path;
    private final Class<? extends WebProcessor> processorClass;
    private final Method processorMethod;

    public WebRouteEntry(String httpMethod, String path, Class<? extends WebProcessor> processorClass, Method processorMethod) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.processorClass = processorClass;
        this.processorMethod = processorMethod;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public Class<? extends WebProcessor> getProcessorClass() {
        return processorClass;
    }

    public Method getProcessorMethod() {
        return processorMethod;
    }
}
