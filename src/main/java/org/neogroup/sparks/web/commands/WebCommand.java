
package org.neogroup.sparks.web.commands;

import org.neogroup.httpserver.HttpRequest;
import org.neogroup.sparks.commands.Command;

/**
 * Web command
 */
public class WebCommand extends Command {

    public static final String CONTEXT_SEPARATOR = "/";

    private final HttpRequest request;

    /**
     * Constructor for the web command
     * @param request http request
     */
    public WebCommand(HttpRequest request) {
        this.request = request;
    }

    /**
     * Get the http request
     * @return http request
     */
    public HttpRequest getRequest() {
        return request;
    }

    /**
     * Get the web route of the command
     * @return string
     */
    public String getWebRoute () {
        String path = request.getPath();
        return path.substring(0, path.lastIndexOf(CONTEXT_SEPARATOR) + 1);
    }

    /**
     * Get the web action of the command
     * @return string
     */
    public String getWebAction () {
        String path = request.getPath();
        return path.substring(path.lastIndexOf(CONTEXT_SEPARATOR) + 1);
    }
}
