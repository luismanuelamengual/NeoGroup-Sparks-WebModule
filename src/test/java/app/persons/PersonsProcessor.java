package app.persons;

import app.persons.Person;
import org.neogroup.httpserver.HttpRequest;
import org.neogroup.httpserver.HttpResponse;
import org.neogroup.sparks.model.EntityPropertyOperator;
import org.neogroup.sparks.model.EntityQuery;
import org.neogroup.sparks.web.processors.WebProcessor;
import org.neogroup.sparks.web.routing.Get;

import java.util.List;

public class PersonsProcessor extends WebProcessor {

    @Get("/persons/test")
    public HttpResponse testAction(HttpRequest request) {

        Person person = new Person();
        person.setId(1);
        person.setName("Ramon");
        person.setLastName("Salgado");
        person.setAge(52);
        createEntity(person);

        person = new Person();
        person.setId(23);
        person.setName("Canduli");
        person.setAge(19);
        updateEntity(person);

        person = new Person();
        person.setId(26);
        person.setName("Vane");
        person.setLastName("Vane");
        person.setAge(33);
        updateEntity(person);

        person = new Person();
        person.setId(30);
        deleteEntity(person);

        EntityQuery query = new EntityQuery();
        query.addSorter("id");
        query.addFilter("age", EntityPropertyOperator.LESS_THAN, 50);
        List<Person> persons = retrieveEntities(Person.class, query);

        StringBuilder personsString = new StringBuilder();
        for (Person p : persons) {
            personsString.append(p);
            personsString.append("<br>");
        }

        return createResponse(personsString.toString());
    }

    @Get("/persons/create")
    public HttpResponse createPersonAction(HttpRequest request) {

        Person person = new Person();
        person.setName(request.getParameter("name"));
        person.setLastName(request.getParameter("lastName"));
        if (request.hasParameter("age")) {
            person.setAge(Integer.parseInt(request.getParameter("age")));
        }
        createEntity(person);
        return createResponse("Person created Ok !!. Id: " + person.getId());
    }
}
