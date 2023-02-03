package org.glassfish.jersey.client.internal;

import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HttpUrlConnectorInternalTest {

    @Test
    public void testBasicDefaultSocketFactoryDetection() throws IOException {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://example.org").openConnection();

        assertTrue(HttpUrlConnector.isDefaultSSLSocketFactory(httpsURLConnection));
    }

    @Test
    public void testContextFieldExtraction() throws IOException {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://example.org").openConnection();

        final Object contextObject = HttpUrlConnector.getContextObject(httpsURLConnection.getSSLSocketFactory());
        final Object defaultContextObject = HttpUrlConnector.getContextObject(HttpsURLConnection.getDefaultSSLSocketFactory());
        assertNotNull(contextObject);
        assertNotNull(defaultContextObject);
        assertSame(defaultContextObject, contextObject);
    }

}
