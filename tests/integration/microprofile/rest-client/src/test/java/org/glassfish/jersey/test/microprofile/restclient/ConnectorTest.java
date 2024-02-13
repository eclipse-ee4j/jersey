/*
 * Copyright (c) 2019, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.microprofile.restclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.LeaseRequest;
import org.apache.hc.core5.util.Timeout;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.glassfish.jersey.apache5.connector.Apache5ClientProperties;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by David Kral.
 */
public class ConnectorTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(ApplicationResourceImpl.class);
    }

    @Test
    public void testConnector() throws URISyntaxException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager() {

            @Override
            public LeaseRequest lease(String id, HttpRoute route, Object state) {
                countDownLatch.countDown();
                return super.lease(id, route, state);
            }

            @Override
            public LeaseRequest lease(String id, HttpRoute route, Timeout requestTimeout, Object state) {
                countDownLatch.countDown();
                return super.lease(id, route, requestTimeout, state);
            }
        };

        ApplicationResource app = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:9998"))
                .property(Apache5ClientProperties.CONNECTION_MANAGER, connectionManager)
                .register(Apache5ConnectorProvider.class)
                .build(ApplicationResource.class);

        app.getTestMap();
        assertEquals(countDownLatch.getCount(), 0);
    }

}
