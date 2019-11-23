package org.glassfish.jersey.internal;

import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static java.math.BigDecimal.ONE;
import static java.net.URI.create;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.glassfish.jersey.internal.ParameterMarshaller.parameterMarshaller;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Reproducer JERSEY-4315
@RunWith(MockitoJUnitRunner.class)
public class EncodedUriBuilderTest {
    private static final URI URI = create("http://localhost");
    private static final String URI_TEMPLATE = "template";
    private static final String SCHEME = "http";
    private static final String USER_INFO = "user info";
    private static final String HOST = "localhost";
    private static final int PORT = 80;
    private static final String PATH = "/message";
    private static final String METHOD = "GET";
    private static final String SEGMENT = "segment";
    private static final String MATRIX = "matrix";
    private static final String NAME = "name";
    private static final String QUERY = "?name=1";
    private static final String FRAGMENT = "fragment";

    @Mock
    private UriBuilder uriBuilder;
    @Spy
    private ParameterMarshaller marshaller = parameterMarshaller(configuration());

    private static Configuration configuration() {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getInstances()).thenReturn(emptySet());
        return configuration;
    }

    @Test
    public void doClone() {
        new EncodedUriBuilder(uriBuilder, marshaller).clone();

        verify(uriBuilder).clone();
    }

    @Test
    public void uri() {
        new EncodedUriBuilder(uriBuilder, marshaller).uri(URI);
        new EncodedUriBuilder(uriBuilder, marshaller).uri(URI_TEMPLATE);

        InOrder inOrder = inOrder(uriBuilder);
        inOrder.verify(uriBuilder).uri(URI);
        inOrder.verify(uriBuilder).uri(URI_TEMPLATE);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void scheme() {
        new EncodedUriBuilder(uriBuilder, marshaller).scheme(SCHEME);

        verify(uriBuilder).scheme(SCHEME);
    }

    @Test
    public void schemeSpecificPart() {
        new EncodedUriBuilder(uriBuilder, marshaller).schemeSpecificPart(SCHEME);

        verify(uriBuilder).schemeSpecificPart(SCHEME);
    }

    @Test
    public void userInfo() {
        new EncodedUriBuilder(uriBuilder, marshaller).userInfo(USER_INFO);

        verify(uriBuilder).userInfo(USER_INFO);
    }

    @Test
    public void host() {
        new EncodedUriBuilder(uriBuilder, marshaller).host(HOST);

        verify(uriBuilder).host(HOST);
    }

    @Test
    public void port() {
        new EncodedUriBuilder(uriBuilder, marshaller).port(PORT);

        verify(uriBuilder).port(PORT);
    }

    @Test
    public void replacePath() {
        new EncodedUriBuilder(uriBuilder, marshaller).replacePath(PATH);

        verify(uriBuilder).replacePath(PATH);
    }

    @Test
    public void path() {
        new EncodedUriBuilder(uriBuilder, marshaller).path(PATH);
        new EncodedUriBuilder(uriBuilder, marshaller).path(MessageResource.class);
        new EncodedUriBuilder(uriBuilder, marshaller).path(MessageResource.class, METHOD);
        new EncodedUriBuilder(uriBuilder, marshaller).path(METHOD);

        InOrder inOrder = inOrder(uriBuilder);
        inOrder.verify(uriBuilder).path(PATH);
        inOrder.verify(uriBuilder).path(MessageResource.class);
        inOrder.verify(uriBuilder).path(MessageResource.class, METHOD);
        inOrder.verify(uriBuilder).path(METHOD);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void segment() {
        new EncodedUriBuilder(uriBuilder, marshaller).segment(SEGMENT);

        verify(uriBuilder).segment(SEGMENT);
    }

    @Test
    public void replaceMatrix() {
        new EncodedUriBuilder(uriBuilder, marshaller).replaceMatrix(MATRIX);

        verify(uriBuilder).replaceMatrix(MATRIX);
    }

    @Test
    public void matrixParam() {
        new EncodedUriBuilder(uriBuilder, marshaller).matrixParam(NAME, ONE);

        verify(uriBuilder).matrixParam(NAME, ONE.toString());
    }

    @Test
    public void replaceMatrixParam() {
        new EncodedUriBuilder(uriBuilder, marshaller).replaceMatrixParam(NAME, ONE);

        verify(uriBuilder).replaceMatrixParam(NAME, ONE.toString());
    }

    @Test
    public void replaceQuery() {
        new EncodedUriBuilder(uriBuilder, marshaller).replaceQuery(QUERY);

        verify(uriBuilder).replaceQuery(QUERY);
    }

    @Test
    public void queryParam() {
        new EncodedUriBuilder(uriBuilder, marshaller).queryParam(NAME, ONE);

        verify(uriBuilder).queryParam(NAME, ONE.toString());
    }

    @Test
    public void replaceQueryParam() {
        new EncodedUriBuilder(uriBuilder, marshaller).replaceQueryParam(NAME, ONE);

        verify(uriBuilder).replaceQueryParam(NAME, ONE.toString());
    }

    @Test
    public void fragment() {
        new EncodedUriBuilder(uriBuilder, marshaller).fragment(FRAGMENT);

        verify(uriBuilder).fragment(FRAGMENT);
    }

    @Test
    public void resolveTemplate() {
        new EncodedUriBuilder(uriBuilder, marshaller).resolveTemplate(NAME, ONE);
        new EncodedUriBuilder(uriBuilder, marshaller).resolveTemplate(NAME, ONE, true);

        InOrder inOrder = inOrder(uriBuilder);
        inOrder.verify(uriBuilder).resolveTemplate(NAME, ONE);
        inOrder.verify(uriBuilder).resolveTemplate(NAME, ONE, true);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void resolveTemplateFromEncoded() {
        new EncodedUriBuilder(uriBuilder, marshaller).resolveTemplateFromEncoded(NAME, ONE);

        verify(uriBuilder).resolveTemplateFromEncoded(NAME, ONE);
    }

    @Test
    public void resolveTemplates() {
        new EncodedUriBuilder(uriBuilder, marshaller).resolveTemplates(singletonMap(NAME, ONE));
        new EncodedUriBuilder(uriBuilder, marshaller).resolveTemplates(singletonMap(NAME, ONE), true);

        InOrder inOrder = inOrder(uriBuilder);
        inOrder.verify(uriBuilder).resolveTemplates(singletonMap(NAME, ONE));
        inOrder.verify(uriBuilder).resolveTemplates(singletonMap(NAME, ONE), true);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void resolveTemplatesFromEncoded() {
        new EncodedUriBuilder(uriBuilder, marshaller).resolveTemplatesFromEncoded(singletonMap(NAME, ONE));

        verify(uriBuilder).resolveTemplatesFromEncoded(singletonMap(NAME, ONE));
    }

    @Test
    public void buildFromMap() {
        new EncodedUriBuilder(uriBuilder, marshaller).buildFromMap(singletonMap(NAME, ONE));
        new EncodedUriBuilder(uriBuilder, marshaller).buildFromMap(singletonMap(NAME, ONE), true);

        InOrder inOrder = inOrder(uriBuilder);
        inOrder.verify(uriBuilder).buildFromMap(singletonMap(NAME, ONE));
        inOrder.verify(uriBuilder).buildFromMap(singletonMap(NAME, ONE), true);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void buildFromEncodedMap() throws IllegalArgumentException, UriBuilderException {
        new EncodedUriBuilder(uriBuilder, marshaller).buildFromEncodedMap(singletonMap(NAME, ONE));

        verify(uriBuilder).buildFromEncodedMap(singletonMap(NAME, ONE));
    }

    @Test
    public void build() throws IllegalArgumentException, UriBuilderException {
        new EncodedUriBuilder(uriBuilder, marshaller).build(ONE);
        new EncodedUriBuilder(uriBuilder, marshaller).build(ONE, true);

        InOrder inOrder = inOrder(uriBuilder);
        inOrder.verify(uriBuilder).build(ONE);
        inOrder.verify(uriBuilder).build(ONE, true);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void buildFromEncoded() throws IllegalArgumentException, UriBuilderException {
        new EncodedUriBuilder(uriBuilder, marshaller).buildFromEncoded(ONE);

        verify(uriBuilder).buildFromEncoded(ONE);
    }

    @Test
    public void toTemplate() {
        new EncodedUriBuilder(uriBuilder, marshaller).toTemplate();

        verify(uriBuilder).toTemplate();
    }

    @Path("/message")
    private static class MessageResource {
        @GET
        @Produces("text/plain")
        public String getMessage(@PathParam("p") String message) {
            return message;
        }
    }
}