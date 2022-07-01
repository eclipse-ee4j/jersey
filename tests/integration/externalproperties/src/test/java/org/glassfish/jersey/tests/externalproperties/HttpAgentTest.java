package org.glassfish.jersey.tests.externalproperties;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.internal.Version;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpAgentTest {

    private final String AGENT = "Custom-agent";
    private final URI BASE_URI = URI.create("http://localhost:9997/");
    private HttpServer server;

    @Test
    public void testHttpAgentSetBySystemProperty() {
        javax.ws.rs.core.Response response = JerseyClientBuilder.newClient()
                .target(BASE_URI)
                .request()
                .header(HttpHeaders.USER_AGENT, null)
                .get();

        Assert.assertTrue(response.readEntity(String.class).contains(AGENT));
    }

    @Test
    public void testUserAgentJerseyHeader() {
        javax.ws.rs.core.Response response = JerseyClientBuilder.newClient()
                .target(BASE_URI)
                .request()
                .get();

        String agentHeader = response.readEntity(String.class);
        Assert.assertFalse(agentHeader.contains(AGENT));
        Assert.assertTrue(agentHeader.contains("Jersey/" + Version.getVersion()));
    }

    @Before
    public void startAgentServer() {
        server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

        try {
            server.start();
        } catch (IOException ioe) {
            throw new ProcessingException("Grizzly server failed to start");
        }

        server.getServerConfiguration().addHttpHandler(new HttpHandler() {
            @Override
            public void service(Request request, Response response) throws Exception {
                String agentHeader = request.getHeader(HttpHeaders.USER_AGENT);
                response.setContentType("text/plain");
                response.setContentLength(agentHeader.length());
                response.getWriter().write(agentHeader);
                response.setStatus(200);
            }
        });
    }

    @After
    public void stopAgentServer() {
        server.shutdownNow();
    }

}