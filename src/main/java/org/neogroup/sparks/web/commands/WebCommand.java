
package org.neogroup.sparks.web.commands;

import org.neogroup.httpserver.HttpRequest;
import org.neogroup.sparks.commands.Command;

/**
 * Web command
 */
public class WebCommand extends Command {

    private final HttpRequest request;

    /**
     * Constructor for the web command
     * @param request http request
     */
    public WebCommand(HttpRequest request) {
        this.request = request;
    }

    /**
     * Retrieves the request associated to the command
     * @return http request
     */
    public HttpRequest getRequest() {
        return request;
    }
}
