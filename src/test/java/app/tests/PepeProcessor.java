package app.tests;

import org.neogroup.httpserver.HttpRequest;
import org.neogroup.httpserver.HttpResponse;
import org.neogroup.sparks.web.processors.WebProcessor;
import org.neogroup.sparks.web.routing.Get;

public class PepeProcessor extends WebProcessor {

    @Get("/pepe")
    public HttpResponse indexAction (HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.setBody("PEPE CONTROLLER !!");
        return response;
    }
}
