
package org.neogroup.sparks.web;

import org.neogroup.httpserver.*;
import org.neogroup.httpserver.contexts.HttpContext;
import org.neogroup.sparks.Application;
import org.neogroup.sparks.Module;
import org.neogroup.sparks.processors.Processor;
import org.neogroup.sparks.web.processors.WebProcessor;
import org.neogroup.sparks.web.routing.*;
import org.neogroup.sparks.web.routing.Error;
import org.neogroup.util.MimeUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Web module for sparks framework
 */
public class WebModule extends Module {

    public static final int DEFAULT_SERVER_PORT = 80;

    private final HttpServer server;
    private final WebRoutes routes;
    private final WebRoutes beforeRoutes;
    private final WebRoutes afterRoutes;
    private final WebRoutes notFoundRoutes;
    private final WebRoutes errorRoutes;

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
        routes = new WebRoutes();
        beforeRoutes = new WebRoutes();
        afterRoutes = new WebRoutes();
        notFoundRoutes = new WebRoutes();
        errorRoutes = new WebRoutes();
        server = new HttpServer();
        server.setProperty("port", port);
        server.addContext(new HttpContext("/") {
            @Override
            public HttpResponse onContext(HttpRequest request) {
                WebRouteEntry webRoute = routes.findWebRoute(request);
                HttpResponse response = null;
                try {
                    if (webRoute != null) {
                        WebRouteEntry beforeWebRoute = beforeRoutes.findWebRoute(request);
                        if (beforeWebRoute != null) {
                            response = (HttpResponse) beforeWebRoute.getProcessorMethod().invoke(getProcessorInstance(beforeWebRoute.getProcessorClass()), request);
                        }
                        if (response == null) {
                            response = (HttpResponse) webRoute.getProcessorMethod().invoke(getProcessorInstance(webRoute.getProcessorClass()), request);

                            WebRouteEntry afterWebRoute = afterRoutes.findWebRoute(request);
                            if (afterWebRoute != null) {
                                response = (HttpResponse) afterWebRoute.getProcessorMethod().invoke(getProcessorInstance(afterWebRoute.getProcessorClass()), request, response);
                            }
                        }
                    }
                    else {
                        response = onRouteNotFound(request);
                    }
                }
                catch (InvocationTargetException invocationException) {
                    response = onError(request, invocationException.getCause());
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

        WebRouteEntry notFoundRoute = notFoundRoutes.findWebRoute(request);
        HttpResponse response = new HttpResponse();
        if (notFoundRoute != null) {
            try {
                response = (HttpResponse) notFoundRoute.getProcessorMethod().invoke(getProcessorInstance(notFoundRoute.getProcessorClass()), request);
            }
            catch (Throwable error) {
                throw new RuntimeException("Error processing not found route !!");
            }
        }
        else {
            response.setResponseCode(HttpResponseCode.HTTP_NOT_FOUND);
            response.addHeader(HttpHeader.CONTENT_TYPE, MimeUtils.TEXT_PLAIN);
            response.setBody("No route found for path \"" + request.getPath() + "\" !!");
        }
        return response;
    }

    /**
     * Method that is executed when a context throws an exception
     * @param request http request
     * @param throwable exception
     * @return http response
     */
    protected HttpResponse onError (HttpRequest request, Throwable throwable) {

        WebRouteEntry errorRoute = errorRoutes.findWebRoute(request);
        HttpResponse response = new HttpResponse();
        if (errorRoute != null) {
            try {
                response = (HttpResponse) errorRoute.getProcessorMethod().invoke(getProcessorInstance(errorRoute.getProcessorClass()), request, throwable);
            }
            catch (Throwable routeError) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PrintStream printer = new PrintStream(out);
                routeError.printStackTrace(printer);
                byte[] body = out.toByteArray();
                response.setResponseCode(HttpResponseCode.HTTP_INTERNAL_ERROR);
                response.addHeader(HttpHeader.CONTENT_TYPE, MimeUtils.TEXT_PLAIN);
                response.setBody(body);
            }
        }
        else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream printer = new PrintStream(out);
            throwable.printStackTrace(printer);
            byte[] body = out.toByteArray();
            response.setResponseCode(HttpResponseCode.HTTP_INTERNAL_ERROR);
            response.addHeader(HttpHeader.CONTENT_TYPE, MimeUtils.TEXT_PLAIN);
            response.setBody(body);
        }
        return response;
    }

    /**
     * Register all the web routes from the web controllers
     */
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
                        for (String path : getAnnotation.value()) {
                            routes.addWebRoute(new WebRouteEntry("GET", path, webProcessorClass, method));
                        }
                    }
                    Post postAnnotation = method.getAnnotation(Post.class);
                    if (postAnnotation != null) {
                        for (String path : postAnnotation.value()) {
                            routes.addWebRoute(new WebRouteEntry("POST", path, webProcessorClass, method));
                        }
                    }
                    Put putAnnotation = method.getAnnotation(Put.class);
                    if (putAnnotation != null) {
                        for (String path : putAnnotation.value()) {
                            routes.addWebRoute(new WebRouteEntry("PUT", path, webProcessorClass, method));
                        }
                    }
                    Delete deleteAnnotation = method.getAnnotation(Delete.class);
                    if (deleteAnnotation != null) {
                        for (String path : deleteAnnotation.value()) {
                            routes.addWebRoute(new WebRouteEntry("DELETE", path, webProcessorClass, method));
                        }
                    }
                    Route routeAnnotation = method.getAnnotation(Route.class);
                    if (routeAnnotation != null) {
                        for (String path : routeAnnotation.value()) {
                            routes.addWebRoute(new WebRouteEntry(null, path, webProcessorClass, method));
                        }
                    }
                    Before beforeAnnotation = method.getAnnotation(Before.class);
                    if (beforeAnnotation != null) {
                        for (String path : beforeAnnotation.value()) {
                            beforeRoutes.addWebRoute(new WebRouteEntry(null, path, webProcessorClass, method));
                        }
                    }
                    After afterAnnotation = method.getAnnotation(After.class);
                    if (afterAnnotation != null) {
                        for (String path : afterAnnotation.value()) {
                            afterRoutes.addWebRoute(new WebRouteEntry(null, path, webProcessorClass, method));
                        }
                    }
                    Error errorAnnotation = method.getAnnotation(Error.class);
                    if (errorAnnotation != null) {
                        for (String path : errorAnnotation.value()) {
                            errorRoutes.addWebRoute(new WebRouteEntry(null, path, webProcessorClass, method));
                        }
                    }
                    NotFound notFoundAnnotation = method.getAnnotation(NotFound.class);
                    if (notFoundAnnotation != null) {
                        for (String path : notFoundAnnotation.value()) {
                            notFoundRoutes.addWebRoute(new WebRouteEntry(null, path, webProcessorClass, method));
                        }
                    }
                }
            }
            catch (ClassCastException ex) {}
        }
    }
}
