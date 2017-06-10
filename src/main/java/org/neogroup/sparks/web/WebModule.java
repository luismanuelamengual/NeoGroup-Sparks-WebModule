
package org.neogroup.sparks.web;

import org.neogroup.httpserver.*;
import org.neogroup.httpserver.contexts.HttpContext;
import org.neogroup.sparks.Application;
import org.neogroup.sparks.Module;
import org.neogroup.sparks.processors.Processor;
import org.neogroup.sparks.web.processors.WebProcessor;
import org.neogroup.sparks.web.routing.*;
import org.neogroup.util.MimeUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Web module for sparks framework
 */
public class WebModule extends Module {

    public static final int DEFAULT_SERVER_PORT = 80;

    private static final String ROUTE_GENERIC_PATH = "*";
    private static final String ROUTE_PARAMETER_PREFIX = ":";
    private static final String ROUTE_PARAMETER_WILDCARD = "%";
    private static final String ROUTE_PATH_SEPARATOR = "/";

    private final HttpServer server;
    private final WebRouteIndex routeIndex;

    /**
     * Constructor for the web module
     * @param application application
     */
    public WebModule(Application application) {
        this(application, DEFAULT_SERVER_PORT);
    }

    /**
     * Constructor for the web module
     * @param application application
     * @param port port for the http server
     */
    public WebModule(Application application, int port) {
        super(application);
        routeIndex = new WebRouteIndex();
        server = new HttpServer();
        server.setProperty("port", port);
        server.addContext(new HttpContext("/") {
            @Override
            public HttpResponse onContext(HttpRequest request) {
                WebRouteEntry webRoute = findWebRoute(request);
                HttpResponse response = null;
                try {
                    if (webRoute != null) {

                        if (webRoute.getPath().contains(ROUTE_PARAMETER_PREFIX)) {
                            String[] pathParts = webRoute.getPath().split(ROUTE_PATH_SEPARATOR);
                            String[] requestParts = request.getPath().split(ROUTE_PATH_SEPARATOR);
                            for (int i = 0; i < pathParts.length; i++) {
                                String pathPart = pathParts[i];
                                if (pathPart.startsWith(ROUTE_PARAMETER_PREFIX)) {
                                    String parameterName = pathPart.substring(1);
                                    String parameterValue = requestParts[i];
                                    request.setParameter(parameterName, parameterValue);
                                }
                            }
                        }

                        Class<? extends WebProcessor> webProcessorClass = webRoute.getProcessorClass();
                        response = (HttpResponse)webRoute.getProcessorMethod().invoke(getProcessorInstance(webProcessorClass), request);
                    } else {
                        response = onRouteNotFound(request);
                    }
                }
                catch (Throwable throwable) {
                    response = onError(request, throwable);
                }
                return response;
            }
        });
    }

    /**
     * Starts the web module
     */
    @Override
    protected void onStart() {
        registerWebRoutes();
        server.start();
    }

    /**
     * Stops the web module
     */
    @Override
    protected void onStop() {
        server.stop();
    }

    /**
     * Adds a http context to the module
     * @param context context
     */
    public void addContext(HttpContext context) {
        server.addContext(context);
    }

    /**
     * Removes a http context from the module
     * @param context context
     */
    public void removeContext(HttpContext context) {
        server.removeContext(context);
    }

    /**
     * Method that is executed when a context was not found
     * @param request Http request
     * @return http response
     */
    protected HttpResponse onRouteNotFound (HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.HTTP_NOT_FOUND);
        response.addHeader(HttpHeader.CONTENT_TYPE, MimeUtils.TEXT_PLAIN);
        response.setBody("No route found for path \"" + request.getPath() + "\" !!");
        return response;
    }

    /**
     * Method that is executed when a context throws an exception
     * @param request http request
     * @param throwable exception
     * @return http response
     */
    protected HttpResponse onError (HttpRequest request, Throwable throwable) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printer = new PrintStream(out);
        throwable.printStackTrace(printer);
        byte[] body = out.toByteArray();
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.HTTP_INTERNAL_ERROR);
        response.addHeader(HttpHeader.CONTENT_TYPE, MimeUtils.TEXT_PLAIN);
        response.setBody(body);
        return response;
    }

    protected void registerWebRoutes () {

        //Retrieve all processors visible from this module/application
        Set<Class<? extends Processor>> registeredProcessors = new HashSet<>();
        registeredProcessors.addAll(getRegisteredProcessors());
        registeredProcessors.addAll(getApplication().getRegisteredProcessors());

        //Register processor class candidates
        for (Class<? extends Processor> processorClass : registeredProcessors) {
            try {
                Class<? extends WebProcessor> webProcessorClass = (Class<? extends WebProcessor>) processorClass;
                for (Method method : webProcessorClass.getDeclaredMethods()) {
                    Get getAnnotation = method.getAnnotation(Get.class);
                    if (getAnnotation != null) {
                        WebRouteEntry route = new WebRouteEntry("GET", getAnnotation.value(), webProcessorClass, method);
                        addWebRoute(route);
                    }
                    Post postAnnotation = method.getAnnotation(Post.class);
                    if (postAnnotation != null) {
                        WebRouteEntry route = new WebRouteEntry("POST", postAnnotation.value(), webProcessorClass, method);
                        addWebRoute(route);
                    }
                    Put putAnnotation = method.getAnnotation(Put.class);
                    if (putAnnotation != null) {
                        WebRouteEntry route = new WebRouteEntry("PUT", putAnnotation.value(), webProcessorClass, method);
                        addWebRoute(route);
                    }
                    Delete deleteAnnotation = method.getAnnotation(Delete.class);
                    if (deleteAnnotation != null) {
                        WebRouteEntry route = new WebRouteEntry("DELETE", deleteAnnotation.value(), webProcessorClass, method);
                        addWebRoute(route);
                    }
                    Request requestAnnotation = method.getAnnotation(Request.class);
                    if (requestAnnotation != null) {
                        WebRouteEntry route = new WebRouteEntry(null, requestAnnotation.value(), webProcessorClass, method);
                        addWebRoute(route);
                    }
                }
            }
            catch (ClassCastException ex) {}
        }
    }

    protected void addWebRoute (WebRouteEntry route) {

        String path = route.getPath();
        String[] pathParts = path.split(ROUTE_PATH_SEPARATOR);
        WebRouteIndex currentRootIndex = routeIndex;
        for (String pathPart : pathParts) {
            if (pathPart.isEmpty()) {
                continue;
            }
            if (pathPart.equals(ROUTE_GENERIC_PATH)) {
                currentRootIndex.addGenericRoute(route);
                break;
            }
            String index = null;
            if (pathPart.startsWith(ROUTE_PARAMETER_PREFIX)) {
                index = ROUTE_PARAMETER_WILDCARD;
            } else {
                index = pathPart;
            }
            WebRouteIndex routeIndex = currentRootIndex.getRouteIndex(index);
            if (routeIndex == null) {
                routeIndex = new WebRouteIndex();
                currentRootIndex.addRouteIndex(index, routeIndex);
            }
            currentRootIndex = routeIndex;
        }
        currentRootIndex.addRoute(route);
    }

    protected WebRouteEntry findWebRoute (HttpRequest request) {

        WebRouteEntry route = null;
        String[] pathParts = request.getPath().split(ROUTE_PATH_SEPARATOR);
        WebRouteIndex currentRootIndex = routeIndex;
        boolean routeFound = true;
        for (String pathPart : pathParts) {
            if (pathPart.isEmpty()) {
                continue;
            }
            WebRouteIndex nextRootIndex = currentRootIndex.getRouteIndex(pathPart);
            if (nextRootIndex == null) {
                nextRootIndex = currentRootIndex.getRouteIndex(ROUTE_PARAMETER_WILDCARD);
                if (nextRootIndex == null) {
                    nextRootIndex = currentRootIndex.getRouteIndex(ROUTE_GENERIC_PATH);
                    if (nextRootIndex == null) {
                        routeFound = false;
                        break;
                    }
                }
            }
            currentRootIndex = nextRootIndex;
        }

        if (routeFound) {
            for (WebRouteEntry routeEntry : currentRootIndex.getRoutes()) {
                if (routeEntry.getHttpMethod() == null || routeEntry.getHttpMethod().equals(request.getMethod())) {
                    route = routeEntry;
                    break;
                }
            }
        }
        return route;
    }
}
