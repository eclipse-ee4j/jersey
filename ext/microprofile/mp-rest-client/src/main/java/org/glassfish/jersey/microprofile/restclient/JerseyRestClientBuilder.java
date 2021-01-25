/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Payara Foundation and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
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
