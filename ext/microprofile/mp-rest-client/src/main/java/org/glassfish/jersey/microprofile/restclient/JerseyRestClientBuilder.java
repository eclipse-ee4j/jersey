package org.glassfish.jersey.microprofile.restclient;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Configuration;
import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.internal.util.collection.UnsafeValue;
import org.glassfish.jersey.internal.util.collection.Values;

public class JerseyRestClientBuilder extends JerseyClientBuilder {

    /**
     * Create a new custom-configured {@link JerseyClient} instance.
     *
     * @return new configured Jersey client instance.
     */
    public static JerseyClient createClient() {
        return new JerseyRestClientBuilder().build();
    }

    /**
     * Create a new custom-configured {@link JerseyClient} instance.
     *
     * @param configuration data used to provide initial configuration for the
     * new Jersey client instance.
     * @return new configured Jersey client instance.
     */
    public static JerseyClient createClient(Configuration configuration) {
        return new JerseyRestClientBuilder().withConfig(configuration).build();
    }

    @Override
    public JerseyClient build() {
        if (sslContext != null) {
            return new JerseyRestClient(config, sslContext, hostnameVerifier);
        } else if (sslConfigurator != null) {
            final SslConfigurator sslConfiguratorCopy = sslConfigurator.copy();
            return new JerseyRestClient(
                    config,
                    Values.lazy((UnsafeValue<SSLContext, IllegalStateException>) sslConfiguratorCopy::createSSLContext),
                    hostnameVerifier);
        } else {
            return new JerseyRestClient(config, (UnsafeValue<SSLContext, IllegalStateException>) null, hostnameVerifier);
        }
    }
}
