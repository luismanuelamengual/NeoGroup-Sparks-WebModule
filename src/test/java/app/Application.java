package app;

import app.persons.PersonCRUDProcessor;
import app.persons.PersonsProcessor;
import app.tests.PepeProcessor;
import app.tests.RamaProcessor;
import app.tests.TestProcessor;
import app.users.UserCRUDProcessor;
import app.users.UsersProcessor;
import org.neogroup.sparks.views.velocity.VelocityViewFactory;
import org.neogroup.sparks.web.WebModule;
import org.postgresql.ds.PGPoolingDataSource;

public class Application extends org.neogroup.sparks.Application {

    public static void main(String[] args) {
        Application application = new Application();
        application.start();
    }

    public Application() {

        loadPropertiesFromResource("app.properties");
        setProperty("defaultBundleName", "localization/messages");

        WebModule module1 = new WebModule(this, 1408);
        module1.registerProcessor(RamaProcessor.class);
        addModule(module1);

        WebModule module2 = new WebModule(this, 1409);
        module2.registerProcessor(PepeProcessor.class);
        addModule(module2);

        //Add view factories
        addViewFactory("velocity", new VelocityViewFactory());

        //Add data sources
        PGPoolingDataSource postgreDataSource = new PGPoolingDataSource();
        postgreDataSource.setServerName("localhost");
        postgreDataSource.setDatabaseName("testdb");
        postgreDataSource.setUser("postgres");
        postgreDataSource.setPassword("postgres");
        addDataSource("main", postgreDataSource);

        //Register Test processors
        registerProcessors(
                TestProcessor.class
        );

        //Register User processors
        registerProcessors(
                UserCRUDProcessor.class,
                UsersProcessor.class
        );

        //Register Person processors
        registerProcessors(
                PersonCRUDProcessor.class,
                PersonsProcessor.class
        );
    }
}
