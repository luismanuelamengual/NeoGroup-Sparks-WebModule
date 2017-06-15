
package org.neogroup.sparks.web.processors;

import org.neogroup.httpserver.HttpHeader;
import org.neogroup.httpserver.HttpResponse;
import org.neogroup.httpserver.HttpResponseCode;
import org.neogroup.sparks.commands.Command;
import org.neogroup.sparks.processors.Processor;
import org.neogroup.sparks.views.View;
import org.neogroup.util.MimeUtils;

/**
 * Web processor for the sparks web module
 */
public abstract class WebProcessor extends Processor {

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
