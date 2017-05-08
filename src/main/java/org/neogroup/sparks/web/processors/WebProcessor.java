
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

public abstract class WebProcessor extends Processor<WebCommand, HttpResponse> {

    private final Map<String, Method> actionMethods;

    public WebProcessor() {
        actionMethods = new HashMap<>();
        for (Method method : getClass().getDeclaredMethods()) {
            RouteAction webAction = method.getAnnotation(RouteAction.class);
            if (webAction != null) {
                actionMethods.put(webAction.name(), method);
            }
        }
    }

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

    protected HttpResponse onBeforeAction (String action, HttpRequest request) {
        return null;
    }

    protected HttpResponse onAfterAction (String action, HttpRequest request, HttpResponse response) {
        return response;
    }

    protected HttpResponse onActionError (String action, HttpRequest request, Throwable throwable) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printer = new PrintStream(out);
        throwable.printStackTrace(printer);
        byte[] body = out.toByteArray();
        return createResponse(HttpResponseCode.HTTP_INTERNAL_ERROR, MimeUtils.TEXT_PLAIN, body);
    }

    protected HttpResponse onActionNotFound (String action, HttpRequest request) {
        return createResponse(HttpResponseCode.HTTP_NOT_FOUND, MimeUtils.TEXT_PLAIN, "Action \"" + action + "\" found in controller \"" + this + "\" !!");
    }

    protected HttpResponse createResponse (String body) {
        return createResponse(HttpResponseCode.HTTP_OK, body);
    }

    protected HttpResponse createResponse (String responseType, String body) {
        return createResponse(HttpResponseCode.HTTP_OK, responseType, body);
    }

    protected HttpResponse createResponse (int responseCode, String body) {
        return createResponse(responseCode, MimeUtils.TEXT_HTML, body);
    }

    protected HttpResponse createResponse (int responseCode, String responseType, String body) {
        return createResponse (responseCode, responseType, body.getBytes());
    }

    protected HttpResponse createResponse (int responseCode, String responseType, byte[] body) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(responseCode);
        response.addHeader(HttpHeader.CONTENT_TYPE, responseType);
        response.setBody(body);
        return response;
    }

    protected HttpResponse createRedirectionResponse (String path) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.HTTP_MOVED_TEMP);
        response.setBody(new byte[0]);
        response.addHeader("location", path);
        return response;
    }

    protected ViewHttpResponse createViewResponse(String viewName) {
        return createViewResponse(HttpResponseCode.HTTP_OK, viewName);
    }

    protected ViewHttpResponse createViewResponse(int responseCode, String viewName) {
        ViewHttpResponse templateResponse = new ViewHttpResponse(createView(viewName));
        templateResponse.setResponseCode(responseCode);
        return templateResponse;
    }

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
        protected void writeBuffer() {
            if (!viewRendered) {
                setBody(view.render());
                viewRendered = true;
            }
            super.writeBuffer();
        }
    }
}
