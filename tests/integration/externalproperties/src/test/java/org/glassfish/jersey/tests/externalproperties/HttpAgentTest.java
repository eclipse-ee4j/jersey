package org.glassfish.jersey.tests.externalproperties;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.glassfish.jersey.ExternalProperties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

public class HttpAgentTest {

    private final String AGENT = "Custom-agent";
    private final String AGENT_HANDLER_URI = "http://localhost:9997/";
    private final int PORT = 9997;
    private Server server;

    @Test
    public void testHttpAgent() {
        Assert.assertEquals(AGENT, System.getProperty(ExternalProperties.HTTP_AGENT));

        Response response = ClientBuilder.newClient().target(AGENT_HANDLER_URI).request().get();

        Assert.assertEquals(200, response.getStatus());
    }

    @Before
    public void startAgentHandler()  {
        server = new Server(PORT);
        server.setHandler(new AgentHandler());
        try {
            server.start();
        } catch (Exception e) {

        }
    }

    @After
    public void stopAgentHandler() {
        try {
            server.stop();
        } catch (Exception e) {

        }
    }

    class AgentHandler extends AbstractHandler {
        @Override
        public void handle(String target,
                           Request baseRequest,
                           HttpServletRequest request,
                           HttpServletResponse response) {

            Assert.assertEquals(AGENT, request.getHeader(HttpHeaders.USER_AGENT).split(" ")[0]);
            response.setStatus(200);
            baseRequest.setHandled(true);
        }
    }

}
