
package app.tests;

import org.neogroup.httpserver.HttpRequest;
import org.neogroup.httpserver.HttpResponse;
import org.neogroup.sparks.web.processors.WebProcessor;
import org.neogroup.sparks.web.routing.Get;

public class RamaProcessor extends WebProcessor {

    @Get("/rama/")
    public HttpResponse indexAction (HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.setBody("RAMA CONTROLLER !!");
        return response;
    }
}
