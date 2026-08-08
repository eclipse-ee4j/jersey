/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.jersey.client.internal;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;

/**
 * Regression tests for {@code sun.net.http.allowRestrictedHeaders} handling after the
 * HttpUrlConnector configuration refactor (issue #6085).
 * <p>
 * {@link HttpUrlConnectorConfiguration.ReadWrite#fromClient} must apply the system property
 * to the configuration instance it returns (the one stored by {@link HttpUrlConnector}),
 * not to the intermediate instance used to build that copy.
 */
public class HttpUrlConnectorRestrictedHeadersTest {

    private static final String PROPERTY =
            HttpUrlConnectorConfiguration.ALLOW_RESTRICTED_HEADERS_SYSTEM_PROPERTY;

    private String previousProperty;

    @AfterEach
    public void restoreSystemProperty() {
        if (previousProperty == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, previousProperty);
        }
    }

    private void captureProperty() {
        previousProperty = System.getProperty(PROPERTY);
    }

    private static HttpUrlConnectorConfiguration.ReadWrite fromClient(Configuration configuration) {
        // Match HttpUrlConnector: config.rw().fromClient(configuration)
        return new HttpUrlConnectorConfiguration.ReadWrite().fromClient(configuration);
    }

    @Test
    public void fromClientRecordsAllowRestrictedHeadersTrueOnReturnedConfig() {
        captureProperty();
        System.setProperty(PROPERTY, "true");

        Client client = JerseyClientBuilder.createClient();
        try {
            HttpUrlConnectorConfiguration.ReadWrite clientConfig = fromClient(client.getConfiguration());

            Assertions.assertTrue(clientConfig.isRestrictedHeaderPropertySet.get(),
                    "fromClient must record allowRestrictedHeaders=true on the returned configuration");
        } finally {
            client.close();
        }
    }

    @Test
    public void fromClientRecordsAllowRestrictedHeadersFalseOnReturnedConfig() {
        captureProperty();
        System.setProperty(PROPERTY, "false");

        Client client = JerseyClientBuilder.createClient();
        try {
            HttpUrlConnectorConfiguration.ReadWrite clientConfig = fromClient(client.getConfiguration());

            Assertions.assertFalse(clientConfig.isRestrictedHeaderPropertySet.get(),
                    "fromClient must record allowRestrictedHeaders=false on the returned configuration");
        } finally {
            client.close();
        }
    }

    @Test
    public void fromClientCopyRetainsValueViaSetNonEmpty() {
        captureProperty();
        System.setProperty(PROPERTY, "true");

        Client client = JerseyClientBuilder.createClient();
        try {
            HttpUrlConnectorConfiguration.ReadWrite clientConfig = fromClient(client.getConfiguration());

            // fromRequest path starts with copy(), which init()s to false then setNonEmpty(clientConfig)
            HttpUrlConnectorConfiguration.ReadWrite copy = clientConfig.copy();
            Assertions.assertTrue(copy.isRestrictedHeaderPropertySet.get(),
                    "client configuration value must survive copy() used by fromRequest()");
        } finally {
            client.close();
        }
    }
}
