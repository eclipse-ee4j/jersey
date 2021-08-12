import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import static org.junit.Assert.fail;

public class JerseyInvocationMethodEntityTest {

    @Test
    public void deleteMethodTest() {
        final Client c1 = ClientBuilder.newClient();
        try {
            c1.target("http://localhost:8080/myPath").request().method("DELETE", Entity.text("body"));
        } catch (ProcessingException e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Test
    public void getMethodTest() {
        final Client c1 = ClientBuilder.newClient();
        try {
            c1.target("http://localhost:8080/myPath").request().method("GET", Entity.text("body"));
        } catch (ProcessingException e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Test
    public void headMethodTest() {
        final Client c1 = ClientBuilder.newClient();
        try {
            c1.target("http://localhost:8080/myPath").request().method("HEAD", Entity.text("body"));
        } catch (ProcessingException e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Test
    public void optionsMethodTest() {
        final Client c1 = ClientBuilder.newClient();
        try {
            WebTarget target = c1.target("http://localhost:8080").path("myPath");
            target.request().method("GET");
        } catch (ProcessingException e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }

    @Test
    public void putMethodTest() {
        final Client c1 = ClientBuilder.newClient();
        try {
            c1.target("http://localhost:8080/myPath").request().method("PUT");
        } catch (ProcessingException e) {
            e.printStackTrace();
            fail("An exception is not expected. " + e.getMessage());
        }
    }
}
