
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

public class WebModule extends Module {

    public static final int DEFAULT_SERVER_PORT = 80;

    private final HttpServer server;

    public WebModule(Application application) {
        this(application, DEFAULT_SERVER_PORT);
    }

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

    @Override
    protected void onStart() {
        server.start();
    }

    @Override
    protected void onStop() {
        server.stop();
    }

    public void addContext(HttpContext context) {
        server.addContext(context);
    }

    public void removeContext(HttpContext context) {
        server.removeContext(context);
    }

    protected HttpResponse onContextNotFound (WebCommand command) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.HTTP_NOT_FOUND);
        response.addHeader(HttpHeader.CONTENT_TYPE, MimeUtils.TEXT_PLAIN);
        response.setBody("No processor found for path \"" + command.getWebRoute() + "\" !!");
        return response;
    }

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
