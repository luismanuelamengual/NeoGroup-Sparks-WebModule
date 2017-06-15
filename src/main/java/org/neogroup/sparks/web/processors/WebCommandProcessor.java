
package org.neogroup.sparks.web.processors;

import org.neogroup.httpserver.HttpRequest;
import org.neogroup.httpserver.HttpResponse;
import org.neogroup.sparks.Application;
import org.neogroup.sparks.Module;
import org.neogroup.sparks.processors.*;
import org.neogroup.sparks.web.commands.WebCommand;
import org.neogroup.sparks.web.routing.*;
import org.neogroup.sparks.web.routing.Error;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@ProcessorCommands(WebCommand.class)
public class WebCommandProcessor extends CommandProcessor<WebCommand> {

    private final Routes routes;
    private final Routes beforeRoutes;
    private final Routes afterRoutes;
    private final Routes notFoundRoutes;
    private final Routes errorRoutes;

    public WebCommandProcessor() {
        routes = new Routes();
        beforeRoutes = new Routes();
        afterRoutes = new Routes();
        notFoundRoutes = new Routes();
        errorRoutes = new Routes();
    }

    @Override
    public void start() {

        //Retrieve all processors visible from this module/application
        Set<Processor> registeredProcessors = new HashSet<>();
        registeredProcessors.addAll(getApplicationContext().getRegisteredProcessors());
        if (getApplicationContext() instanceof Module) {
            Module module = (Module)getApplicationContext();
            registeredProcessors.addAll(module.getApplication().getRegisteredProcessors());
        }
        else if (getApplicationContext() instanceof Application) {
            Application application = (Application)getApplicationContext();
            for (Module module : application.getModules()) {
                registeredProcessors.addAll(module.getRegisteredProcessors());
            }
        }

        //Register console processors
        for (Processor processor : registeredProcessors) {
            if (processor instanceof WebProcessor) {
                WebProcessor webProcessor = (WebProcessor)processor;
                for (Method method : webProcessor.getClass().getDeclaredMethods()) {
                    Get getAnnotation = method.getAnnotation(Get.class);
                    if (getAnnotation != null) {
                        for (String path : getAnnotation.value()) {
                            routes.addRoute(new RouteEntry("GET", path, webProcessor, method));
                        }
                    }
                    Post postAnnotation = method.getAnnotation(Post.class);
                    if (postAnnotation != null) {
                        for (String path : postAnnotation.value()) {
                            routes.addRoute(new RouteEntry("POST", path, webProcessor, method));
                        }
                    }
                    Put putAnnotation = method.getAnnotation(Put.class);
                    if (putAnnotation != null) {
                        for (String path : putAnnotation.value()) {
                            routes.addRoute(new RouteEntry("PUT", path, webProcessor, method));
                        }
                    }
                    Delete deleteAnnotation = method.getAnnotation(Delete.class);
                    if (deleteAnnotation != null) {
                        for (String path : deleteAnnotation.value()) {
                            routes.addRoute(new RouteEntry("DELETE", path, webProcessor, method));
                        }
                    }
                    Route routeAnnotation = method.getAnnotation(Route.class);
                    if (routeAnnotation != null) {
                        for (String path : routeAnnotation.value()) {
                            routes.addRoute(new RouteEntry(null, path, webProcessor, method));
                        }
                    }
                    Before beforeAnnotation = method.getAnnotation(Before.class);
                    if (beforeAnnotation != null) {
                        for (String path : beforeAnnotation.value()) {
                            beforeRoutes.addRoute(new RouteEntry(null, path, webProcessor, method));
                        }
                    }
                    After afterAnnotation = method.getAnnotation(After.class);
                    if (afterAnnotation != null) {
                        for (String path : afterAnnotation.value()) {
                            afterRoutes.addRoute(new RouteEntry(null, path, webProcessor, method));
                        }
                    }
                    Error errorAnnotation = method.getAnnotation(Error.class);
                    if (errorAnnotation != null) {
                        for (String path : errorAnnotation.value()) {
                            errorRoutes.addRoute(new RouteEntry(null, path, webProcessor, method));
                        }
                    }
                    NotFound notFoundAnnotation = method.getAnnotation(NotFound.class);
                    if (notFoundAnnotation != null) {
                        for (String path : notFoundAnnotation.value()) {
                            notFoundRoutes.addRoute(new RouteEntry(null, path, webProcessor, method));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Object process(WebCommand command) throws ProcessorException {

        HttpRequest request = command.getRequest();
        RouteEntry route = routes.findRoute(request);
        HttpResponse response = null;

        if (route != null) {
            try {
                RouteEntry beforeRoute = beforeRoutes.findRoute(request);
                if (beforeRoute != null) {
                    response = (HttpResponse) beforeRoute.getProcessorMethod().invoke(beforeRoute.getProcessor(), request);
                }
                if (response == null) {
                    response = (HttpResponse) route.getProcessorMethod().invoke(route.getProcessor(), request);
                    RouteEntry afterWebRoute = afterRoutes.findRoute(request);
                    if (afterWebRoute != null) {
                        response = (HttpResponse) afterWebRoute.getProcessorMethod().invoke(afterWebRoute.getProcessor(), request, response);
                    }
                }
            }
            catch (Throwable throwable) {
                RouteEntry errorRoute = errorRoutes.findRoute(request);
                if (errorRoute != null) {
                    try {
                        response = (HttpResponse) errorRoute.getProcessorMethod().invoke(errorRoute.getProcessor(), request, throwable);
                    }
                    catch (Throwable error) {
                        throw new ProcessorException("Error processing error route !!", error);
                    }
                }
                else {
                    throw new ProcessorException("Error processing request !!", throwable);
                }
            }
        }
        else {
            RouteEntry notFoundRoute = notFoundRoutes.findRoute(request);
            if (notFoundRoute != null) {
                try {
                    response = (HttpResponse) notFoundRoute.getProcessorMethod().invoke(notFoundRoute.getProcessor(), request);
                }
                catch (Throwable error) {
                    throw new ProcessorException("Error processing not found route !!", error);
                }
            }
            else {
                throw new ProcessorNotFoundException("No processor found for request !!");
            }
        }
        return response;
    }
}
