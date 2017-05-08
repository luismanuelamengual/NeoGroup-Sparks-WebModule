
package org.neogroup.sparks.web.commands;

import org.neogroup.httpserver.HttpRequest;
import org.neogroup.sparks.commands.Command;

public class WebCommand extends Command {

    public static final String CONTEXT_SEPARATOR = "/";

    private final HttpRequest request;

    public WebCommand(HttpRequest request) {
        this.request = request;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public String getWebRoute () {
        String path = request.getPath();
        return path.substring(0, path.lastIndexOf(CONTEXT_SEPARATOR) + 1);
    }

    public String getWebAction () {
        String path = request.getPath();
        return path.substring(path.lastIndexOf(CONTEXT_SEPARATOR) + 1);
    }
}
