/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.osgi.test.basic;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.osgi.test.util.Helper;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Abstract JSON OSGi integration test.
 *
 * @author Michal Gajdos
 */
@RunWith(PaxExam.class)
public abstract class AbstractJsonOsgiIntegrationTest {

    private static final String CONTEXT = "/jersey";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(Helper.getPort()).path(CONTEXT).build();

    protected abstract Feature getJsonProviderFeature();

    @Test
    public void testJson() {
        final Feature jsonProviderFeature = getJsonProviderFeature();
        final Client client = ClientBuilder.newClient();
        final ResourceConfig resourceConfig = new ResourceConfig(JsonResource.class);

        if (jsonProviderFeature != null) {
            client.register(jsonProviderFeature);
            resourceConfig.register(jsonProviderFeature);
        }

        HttpServer server = null;
        try {
            server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);

            final String result = client.target(baseUri).path("/json").request(MediaType.APPLICATION_JSON).get(String.class);

            System.out.println("RESULT = " + result);
            assertThat(result, containsString("Jim"));
        } finally {
            if (server != null) {
                server.shutdownNow();
            }
        }
    }
}
