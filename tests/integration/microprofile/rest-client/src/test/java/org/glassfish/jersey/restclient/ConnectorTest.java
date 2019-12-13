package org.glassfish.jersey.restclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by David Kral.
 */
public class ConnectorTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(ApplicationResourceImpl.class);
    }

    @Test
    public void testConnector() throws URISyntaxException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager() {

            @Override
            public ConnectionRequest requestConnection(HttpRoute route, Object state) {
                countDownLatch.countDown();
                return super.requestConnection(route, state);
            }

        };

        ApplicationResource app = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:9998"))
                .property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager)
                .register(ApacheConnectorProvider.class)
                .build(ApplicationResource.class);

        app.getTestMap();
        assertEquals(countDownLatch.getCount(), 0);
    }

}
