
package app.tests;

import org.neogroup.httpserver.HttpRequest;
import org.neogroup.httpserver.HttpResponse;
import org.neogroup.sparks.web.processors.WebProcessor;
import org.neogroup.sparks.web.routing.*;
import org.neogroup.sparks.web.routing.Error;

import java.util.Locale;

public class TestProcessor extends WebProcessor {

    @Get("/test/")
    public HttpResponse indexAction (HttpRequest request) {
        return createResponse("TEST CONTROLLER !!");
    }

    @Get("/test/template")
    public HttpResponse templateAction (HttpRequest request) {
        ViewHttpResponse response  = createViewResponse("templates.helloworld");
        response.setParameter("name", request.getParameter("name"));
        return response;
    }

    @Get("/test/properties")
    public HttpResponse propertiesAction (HttpRequest request) {
        String property = request.getParameter("property");
        String value = (String)getProperty(property);
        return createResponse("El valor de la propiedad \"" + property + "\" es: " + value);
    }

    @Get("/test/bundles")
    public HttpResponse bundlesAction (HttpRequest request) {
        return createResponse(getString(Locale.ENGLISH, "welcome_phrase", "Luis"));
    }

    @Get("/test/vars/:name/:lastname")
    public HttpResponse varsAction (HttpRequest request) {
        return createResponse ("Name: " + request.getParameter("name") + "; LastName: " + request.getParameter("lastname") + " !!");
    }

    @Get("error")
    public HttpResponse errorAction (HttpRequest request) {
        int a = 10 / 0;
        return createResponse ("Error !!");
    }

    @Get("/dimpler/*")
    public HttpResponse dimplerAction (HttpRequest request) {
        return createResponse ("Dimpler generic !!");
    }

    @Get("test/*")
    public HttpResponse testAction (HttpRequest request) {
        return createResponse ("Test generic !!");
    }

    @Get("*")
    public HttpResponse defaultAction (HttpRequest request) {
        return createResponse ("Testing default response !!");
    }

    @Before("/dimpler/*")
    public HttpResponse beforeAction (HttpRequest request) {
        System.out.println ("before action");
        return null;
    }

    @After("/dimpler/*")
    public HttpResponse afterAction (HttpRequest request, HttpResponse response) {
        System.out.println ("after action");
        return response;
    }

    @Get({"multiple", "/test/mul/:name"})
    @Post("postMultiple")
    public HttpResponse multiAction (HttpRequest request) {
        return createResponse("Multiple Action !!");
    }

    @Error("*")
    public HttpResponse errorAction (HttpRequest request, Throwable throwable) {
        return createResponse("sipi, estamos en un error " + throwable.getMessage() + " !!!");
    }
}
