
package app.users;

import org.neogroup.httpserver.HttpHeader;
import org.neogroup.httpserver.HttpRequest;
import org.neogroup.httpserver.HttpResponse;
import app.users.User;
import org.neogroup.sparks.web.processors.WebProcessor;
import org.neogroup.sparks.web.routing.Get;
import org.neogroup.util.MimeUtils;

import java.util.List;

public class UsersProcessor extends WebProcessor {

    @Get("/users/create")
    public HttpResponse createUserAction(HttpRequest request) {

        User user = new User();
        user.setName(request.getParameter("name"));
        user.setLastName(request.getParameter("lastName"));
        if (request.hasParameter("age")) {
            user.setAge(Integer.parseInt(request.getParameter("age")));
        }
        createEntity(user);
        return showUsersAction(request);
    }

    @Get("/users/list")
    public HttpResponse showUsersAction(HttpRequest request) {

        StringBuilder str = new StringBuilder();
        str.append("USERS<br>");
        str.append("=============<br>");
        List<User> users = retrieveEntities(User.class);
        for (User user : users) {
            str.append("Name: ").append(user.getName());
            str.append("|");
            str.append("LastName: ").append(user.getLastName());
            str.append("|");
            str.append("age: ").append(user.getAge());
            str.append("<br>");
        }

        HttpResponse response = new HttpResponse();
        response.addHeader(HttpHeader.CONTENT_TYPE, MimeUtils.TEXT_HTML);
        response.setBody(str.toString());
        return response;
    }

    @Get("/users/show/:id")
    public HttpResponse showUserAction(HttpRequest request) {
        int userId = Integer.parseInt(request.getParameter("id"));
        User user = retrieveEntity(User.class, userId);
        HttpResponse response = new HttpResponse();
        response.addHeader(HttpHeader.CONTENT_TYPE, MimeUtils.TEXT_HTML);
        response.setBody(user.getName());
        return response;
    }
}
