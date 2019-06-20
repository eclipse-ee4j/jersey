package org.glassfish.jersey.restclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.json.Json;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Created by David Kral.
 */
public class ConsumesAndProducesTest extends JerseyTest {
    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(ApplicationResourceImpl.class);
    }

    @Test
    public void testWithEntity() throws URISyntaxException {
        ApplicationResource app = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:9998"))
                .register(new TestClientRequestFilter(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON))
                .build(ApplicationResource.class);

        app.someJsonOperation(Json.createValue(1));
    }

    @Test
    public void testWithoutEntity() throws URISyntaxException {
        ApplicationResource app = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:9998"))
                .register(new TestClientRequestFilter(MediaType.APPLICATION_JSON, MediaType.WILDCARD))
                .build(ApplicationResource.class);

        app.jsonValue();
    }

    private class TestClientRequestFilter implements ClientRequestFilter {

        private final String expectedAccept;
        private final String expectedContentType;

        TestClientRequestFilter(String expectedAccept, String expectedContentType) {
            this.expectedAccept = expectedAccept;
            this.expectedContentType = expectedContentType;
        }

        @Override
        public void filter(ClientRequestContext requestContext) {
            assertTrue(requestContext.getHeaders().containsKey("Accept"));
            List<Object> accept = requestContext.getHeaders().get("Accept");
            if (!accept.contains(expectedAccept) && !accept.contains(MediaType.valueOf(expectedAccept))) {
                fail();
            }

            assertTrue(requestContext.getHeaders().containsKey("Content-Type"));
            List<Object> contentType = requestContext.getHeaders().get("Content-Type");
            assertEquals(contentType.size(), 1);
            if (!contentType.contains(expectedContentType) && !contentType.contains(MediaType.valueOf(expectedContentType))) {
                fail();
            }

            requestContext.abortWith(Response.ok().build());
        }
    }

}
