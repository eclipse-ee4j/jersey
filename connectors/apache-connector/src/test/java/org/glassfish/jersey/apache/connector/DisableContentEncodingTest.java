/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache.connector;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.apache.http.client.config.RequestConfig;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Ondrej Kosatka (ondrej.kosatka at oracle.com)
 */
public class DisableContentEncodingTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Path("/")
    public static class Resource {

        @GET
        public String get(@HeaderParam("Accept-Encoding") String enc) {
            return enc;
        }
    }

    @Test
    public void testDisabledByRequestConfig() {
        ClientConfig cc = new ClientConfig(GZipEncoder.class);
        final RequestConfig requestConfig = RequestConfig.custom().setContentCompressionEnabled(false).build();
        cc.property(ApacheClientProperties.REQUEST_CONFIG, requestConfig);
        cc.connectorProvider(new ApacheConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri());

        String enc = r.request().get().readEntity(String.class);
        assertEquals("", enc);
    }

    @Test
    public void testEnabledByRequestConfig() {
        ClientConfig cc = new ClientConfig(GZipEncoder.class);
        final RequestConfig requestConfig = RequestConfig.custom().setContentCompressionEnabled(true).build();
        cc.property(ApacheClientProperties.REQUEST_CONFIG, requestConfig);
        cc.connectorProvider(new ApacheConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri());

        String enc = r.request().get().readEntity(String.class);
        assertEquals("gzip,deflate", enc);
    }

    @Test
    public void testDefaultEncoding() {
        ClientConfig cc = new ClientConfig(GZipEncoder.class);
        cc.connectorProvider(new ApacheConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri());

        String enc = r.request().get().readEntity(String.class);
        assertEquals("gzip,deflate", enc);
    }

    @Test
    public void testDefaultEncodingOverridden() {
        ClientConfig cc = new ClientConfig(GZipEncoder.class);
        cc.connectorProvider(new ApacheConnectorProvider());
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri());

        String enc = r.request().acceptEncoding("gzip").get().readEntity(String.class);
        assertEquals("gzip", enc);
    }

}
