
package org.neogroup.sparks.web.processors;

import org.neogroup.httpserver.HttpHeader;
import org.neogroup.httpserver.HttpRequest;
import org.neogroup.httpserver.HttpResponse;
import org.neogroup.httpserver.HttpResponseCode;
import org.neogroup.sparks.processors.Processor;
import org.neogroup.sparks.views.View;
import org.neogroup.sparks.web.commands.WebCommand;
import org.neogroup.sparks.web.routing.RouteAction;
import org.neogroup.util.MimeUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Web processor for the sparks web module
 */
public abstract class WebProcessor extends Processor<WebCommand, HttpResponse> {

    private final Map<String, Method> actionMethods;

    /**
     * Constructor for the web processor
     */
    public WebProcessor() {
        actionMethods = new HashMap<>();
        for (Method method : getClass().getDeclaredMethods()) {
            RouteAction webAction = method.getAnnotation(RouteAction.class);
            if (webAction != null) {
                actionMethods.put(webAction.name(), method);
            }
        }
    }

    /**
     * Processes a web command
     * @param command command to process
     * @return http response
     */
    @Override
    public final HttpResponse process(WebCommand command) {

        HttpResponse response = null;
        HttpRequest request = command.getRequest();
        String action = command.getWebAction();
        try {
            Method method = actionMethods.get(action);
            if (method != null) {
                response = onBeforeAction(action, request);
                if (response == null) {
                    response = (HttpResponse)method.invoke(this, request);
                    response = onAfterAction(action, request, response);
                }
            }
            else {
                response = onActionNotFound(action, request);
            }
        }
        catch (Throwable throwable) {
            response = onActionError(action, request, throwable);
        }
        return response;
    }

    /**
     * Method that is executed before the http action
     * @param action action
     * @param request http request
     * @return http response
     */
    protected HttpResponse onBeforeAction (String action, HttpRequest request) {
        return null;
    }

    /**
     * Method that is executed after the http action
     * @param action action
     * @param request http request
     * @param response http response
     * @return http response
     */
    protected HttpResponse onAfterAction (String action, HttpRequest request, HttpResponse response) {
        return response;
    }

    /**
     * Method that is executed when a web processor throws an exception
     * @param action action
     * @param request http request
     * @param throwable exception
     * @return http response
     */
    protected HttpResponse onActionError (String action, HttpRequest request, Throwable throwable) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printer = new PrintStream(out);
        throwable.printStackTrace(printer);
        byte[] body = out.toByteArray();
        return createResponse(HttpResponseCode.HTTP_INTERNAL_ERROR, MimeUtils.TEXT_PLAIN, body);
    }

    /**
     * Method that is executed when an action was not found
     * @param action action
     * @param request http request
     * @return http response
     */
    protected HttpResponse onActionNotFound (String action, HttpRequest request) {
        return createResponse(HttpResponseCode.HTTP_NOT_FOUND, MimeUtils.TEXT_PLAIN, "Action \"" + action + "\" found in controller \"" + this + "\" !!");
    }

    /**
     * Creates a response
     * @param body response body
     * @return http response
     */
    protected HttpResponse createResponse (String body) {
        return createResponse(HttpResponseCode.HTTP_OK, body);
    }

    /**
     * Creates a response
     * @param responseType Response mime type
     * @param body response body
     * @return http response
     */
    protected HttpResponse createResponse (String responseType, String body) {
        return createResponse(HttpResponseCode.HTTP_OK, responseType, body);
    }

    /**
     * Creates a response
     * @param responseCode response code
     * @param body response body
     * @return http response
     */
    protected HttpResponse createResponse (int responseCode, String body) {
        return createResponse(responseCode, MimeUtils.TEXT_HTML, body);
    }

    /**
     * Creates a response
     * @param responseCode response code
     * @param responseType response mime type
     * @param body response body
     * @return http response
     */
    protected HttpResponse createResponse (int responseCode, String responseType, String body) {
        return createResponse (responseCode, responseType, body.getBytes());
    }

    /**
     * Creates a response
     * @param responseCode response code
     * @param responseType response mime type
     * @param body response body
     * @return http response
     */
    protected HttpResponse createResponse (int responseCode, String responseType, byte[] body) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(responseCode);
        response.addHeader(HttpHeader.CONTENT_TYPE, responseType);
        response.setBody(body);
        return response;
    }

    /**
     * Creates a redirection response
     * @param path path to redirect
     * @return http response
     */
    protected HttpResponse createRedirectionResponse (String path) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.HTTP_MOVED_TEMP);
        response.setBody(new byte[0]);
        response.addHeader("location", path);
        return response;
    }

    /**
     * Creates a view response
     * @param viewName name of the view
     * @return http response
     */
    protected ViewHttpResponse createViewResponse(String viewName) {
        ViewHttpResponse templateResponse = new ViewHttpResponse(createView(viewName));
        templateResponse.setResponseCode(HttpResponseCode.HTTP_OK);
        return templateResponse;
    }

    /**
     * Creates a view response
     * @param viewFactoryName name of view factory
     * @param viewName name of the view
     * @return http response
     */
    protected ViewHttpResponse createViewResponse(String viewFactoryName, String viewName) {
        ViewHttpResponse templateResponse = new ViewHttpResponse(createView(viewFactoryName, viewName));
        templateResponse.setResponseCode(HttpResponseCode.HTTP_OK);
        return templateResponse;
    }

    /**
     * Http View Response
     */
    protected class ViewHttpResponse extends HttpResponse {

        private final View view;
        private boolean viewRendered;

        public ViewHttpResponse(View view) {
            this.view = view;
            viewRendered = false;
        }

        public void setParameter(String name, Object value) {
            view.setParameter(name, value);
        }

        public Object getParameter(String name) {
            return view.getParameter(name);
        }

        @Override
        public void flush() {
            if (!viewRendered) {
                setBody(view.render());
                viewRendered = true;
            }
            super.flush();
        }
    }
}
