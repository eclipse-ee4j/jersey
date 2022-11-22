package org.glassfish.jersey.tests.integration.servlet_40_mvc_1;

import org.glassfish.jersey.message.BrotliEncoder;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BrotliITCase extends TestSupport {

    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String BR = "br";

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

}