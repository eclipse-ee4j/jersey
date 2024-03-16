package org.glassfish.jersey.message;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrotliITTest extends JerseyTest {

    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String BR = "br";

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new MyApplication();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    protected void assertHtmlResponse(final String response) {
        assertNotNull(response, "No text returned!");

        assertResponseContains(response, "<html>");
        assertResponseContains(response, "</html>");
    }

    protected void assertResponseContains(final String response, final String text) {
        assertTrue(response.contains(text), "Response should contain " + text + " but was: " + response);
    }

    @Test
    public void testString() throws Exception {
        Response response = target("/client/string")
                .register(BrotliEncoder.class)
                .request("text/html")
                .acceptEncoding(BR)
                .get();
        String resp = response.readEntity(String.class);
        assertResponseContains(resp, "string string string string string string");
        assertEquals(BR, response.getHeaderString(CONTENT_ENCODING));
    }

    @Test
    public void testJsp() throws Exception {
        Response response = target("/client/html")
                .register(BrotliEncoder.class)
                .request("text/html", "application/xhtml+xml", "application/xml;q=0.9", "*/*;q=0.8")
                .acceptEncoding(BR)
                .get();
        String resp = response.readEntity(String.class);
        assertHtmlResponse(resp);
        assertResponseContains(resp, "find this string");
        assertEquals(BR, response.getHeaderString(CONTENT_ENCODING));
    }

    @Test
    public void testJspNotDecoded() throws Exception {
        Response response = target("/client/html")
                .request("text/html", "application/xhtml+xml", "application/xml;q=0.9", "*/*;q=0.8")
                .acceptEncoding(BR)
                .get();
        String resp = response.readEntity(String.class);
        assertFalse(resp.contains("find this string"));
        assertEquals(BR, response.getHeaderString(CONTENT_ENCODING));
    }

    class MyApplication extends ResourceConfig {

        public MyApplication() {
            property("jersey.config.server.mvc.templateBasePath.jsp", "/WEB-INF/jsp");
            property("jersey.config.servlet.filter.forwardOn404", "true");
            property("jersey.config.servlet.filter.staticContentRegex", "/WEB-INF/.*\\.jsp");
            packages(MyApplication.class.getPackage().getName());
            EncodingFilter.enableFor(this, new Class[] {BrotliEncoder.class});
        }
    }

}