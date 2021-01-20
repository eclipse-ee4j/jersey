package org.glassfish.jersey.microprofile.restclient;

import java.net.URI;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.microprofile.restclient.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.collection.UnsafeValue;

public class JerseyRestClient extends JerseyClient {

    /**
     * Create a new Jersey client instance.
     *
     * @param config jersey client configuration.
     * @param sslContext jersey client SSL context.
     * @param verifier jersey client host name verifier.
     */
    protected JerseyRestClient(final Configuration config,
            final SSLContext sslContext,
            final HostnameVerifier verifier) {

        super(config, sslContext, verifier, null);
    }

    /**
     * Create a new Jersey client instance.
     *
     * @param config jersey client configuration.
     * @param sslContextProvider jersey client SSL context provider.
     * @param verifier jersey client host name verifier.
     */
    protected JerseyRestClient(final Configuration config,
            final UnsafeValue<SSLContext, IllegalStateException> sslContextProvider,
            final HostnameVerifier verifier) {
        super(config, sslContextProvider, verifier, null);
    }

    @Override
    public JerseyWebTarget target(final String uri) {
        checkNotClosed();
        checkNotNull(uri, LocalizationMessages.CLIENT_URI_TEMPLATE_NULL());
        return new RestClientWebTarget(uri, this);
    }

    @Override
    public JerseyWebTarget target(final URI uri) {
        checkNotClosed();
        checkNotNull(uri, LocalizationMessages.CLIENT_URI_NULL());
        return new RestClientWebTarget(uri, this);
    }

    @Override
    public JerseyWebTarget target(final UriBuilder uriBuilder) {
        checkNotClosed();
        checkNotNull(uriBuilder, LocalizationMessages.CLIENT_URI_BUILDER_NULL());
        return new RestClientWebTarget(uriBuilder, this);
    }

    @Override
    public JerseyWebTarget target(final Link link) {
        checkNotClosed();
        checkNotNull(link, LocalizationMessages.CLIENT_TARGET_LINK_NULL());
        return new RestClientWebTarget(link, this);
    }

    @Override
    public JerseyInvocation.Builder invocation(final Link link) {
        checkNotClosed();
        checkNotNull(link, LocalizationMessages.CLIENT_INVOCATION_LINK_NULL());
        final RestClientWebTarget t = new RestClientWebTarget(link, this);
        final String acceptType = link.getType();
        return (acceptType != null) ? t.request(acceptType) : t.request();
    }

    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

}
