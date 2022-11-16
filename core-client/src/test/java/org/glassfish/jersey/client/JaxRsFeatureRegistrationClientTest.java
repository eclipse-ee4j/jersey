/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import org.glassfish.jersey.CommonProperties;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class JaxRsFeatureRegistrationClientTest {

    private static final String REGISTERED_FEATURE_RESPONSE = "RegisteredFeature";

    private static final ClientRequestFilter component =
            requestContext -> requestContext
                    .abortWith(Response.status(400).entity(REGISTERED_FEATURE_RESPONSE).build());

    public static class FeatureClientImpl implements Feature {

        @Override
        public boolean configure(FeatureContext context) {
            if ("true".equals(context.getConfiguration().getProperty("runWithJaxRsClient"))) {
                context.register(component);
            }
            return true;
        }
    }

    @Test
    public void featureRegistrationTest() {
        final ClientConfig config = new ClientConfig().property("runWithJaxRsClient", "true");
        final Client client = ClientBuilder.newClient(config);
        final Invocation.Builder request = client.target("").request();

        assertEquals(REGISTERED_FEATURE_RESPONSE, request.get().readEntity(String.class));

        client.close();
    }

    @Test
    public void featureNotRegistrationTest() {
        final ClientConfig config = new ClientConfig()
                .property(CommonProperties.JAXRS_SERVICE_LOADING_ENABLE, false);
        final Client client = ClientBuilder.newClient(config);
        final Invocation.Builder request = client.target("").request();

        assertFalse(client.getConfiguration().isRegistered(FeatureClientImpl.class));

        client.close();
    }

}