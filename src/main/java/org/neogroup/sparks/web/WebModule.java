
package org.neogroup.sparks.web;

import org.neogroup.httpserver.*;
import org.neogroup.httpserver.contexts.HttpContext;
import org.neogroup.sparks.Application;
import org.neogroup.sparks.Module;
import org.neogroup.sparks.processors.ProcessorNotFoundException;
import org.neogroup.sparks.web.commands.WebCommand;
import org.neogroup.sparks.web.processors.WebSelectorProcessor;
import org.neogroup.util.MimeUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Web module for sparks framework
 */
public class WebModule extends Module {

    public static final int DEFAULT_SERVER_PORT = 80;

    private final HttpServer server;

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
        server = new HttpServer(port);
        server.addContext(new HttpContext("/") {
            @Override
            public HttpResponse onContext(HttpRequest request) {
                WebCommand webCommand = new WebCommand(request);
                HttpResponse response = null;
                try {
                    response = processCommand(webCommand);
                }
                catch (ProcessorNotFoundException exception) {
                    response = onContextNotFound(webCommand);
                }
                catch (Throwable throwable) {
                    response = onError(webCommand, throwable);
                }
                return response;
            }
        });
        registerProcessor(WebSelectorProcessor.class);
    }

    /**
     * Starts the web module
     */
    @Override
    protected void onStart() {
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
     * @param command Web command
     * @return http response
     */
    protected HttpResponse onContextNotFound (WebCommand command) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.HTTP_NOT_FOUND);
        response.addHeader(HttpHeader.CONTENT_TYPE, MimeUtils.TEXT_PLAIN);
        response.setBody("No processor found for path \"" + command.getWebRoute() + "\" !!");
        return response;
    }

    /**
     * Method that is executed when a context throws an exception
     * @param command Web command
     * @param throwable exception
     * @return http response
     */
    protected HttpResponse onError (WebCommand command, Throwable throwable) {
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
}
