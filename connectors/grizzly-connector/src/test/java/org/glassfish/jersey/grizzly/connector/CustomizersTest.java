/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.grizzly.connector;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.text;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.ning.http.client.filter.RequestFilter;

/**
 * Async HTTP Client Config and Request customizers unit tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class CustomizersTest extends JerseyTest {

    @Path("/test")
    public static class EchoResource {
        @POST
        public Response post(@HeaderParam("X-Test-Config") String testConfigHeader,
                           @HeaderParam("X-Test-Request") String testRequestHeader,
                           String entity) {
            return Response.ok("POSTed " + entity)
                    .header("X-Test-Config", testConfigHeader)
                    .header("X-Test-Request", testRequestHeader)
                    .build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(EchoResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        final GrizzlyConnectorProvider connectorProvider = new GrizzlyConnectorProvider(
                new GrizzlyConnectorProvider.AsyncClientCustomizer() {
                    @Override
                    public AsyncHttpClientConfig.Builder customize(Client client,
                                                                   Configuration config,
                                                                   AsyncHttpClientConfig.Builder configBuilder) {
                        return configBuilder.addRequestFilter(new RequestFilter() {
                            @Override
                            public FilterContext filter(FilterContext filterContext) throws FilterException {
                                filterContext.getRequest().getHeaders().add("X-Test-Config", "tested");
                                return filterContext;
                            }
                        });
                    }
                });
        config.connectorProvider(connectorProvider);
        GrizzlyConnectorProvider.register(config, new GrizzlyConnectorProvider.RequestCustomizer() {
            @Override
            public RequestBuilder customize(ClientRequest requestContext, RequestBuilder requestBuilder) {
                requestBuilder.addHeader("X-Test-Request", "tested-global");
                return requestBuilder;
            }
        });
    }

    /**
     * Jersey-2540 related test.
     */
    @Test
    public void testCustomizers() {
        Response response;

        // now using global request customizer
        response = target("test").request().post(text("echo"));
        assertEquals("POSTed echo", response.readEntity(String.class));
        assertEquals("tested", response.getHeaderString("X-Test-Config"));
        assertEquals("tested-global", response.getHeaderString("X-Test-Request"));


        // now using request-specific request customizer
        final Invocation.Builder builder = target("test").request();
        GrizzlyConnectorProvider.register(builder, new GrizzlyConnectorProvider.RequestCustomizer() {
            @Override
            public RequestBuilder customize(ClientRequest requestContext, RequestBuilder requestBuilder) {
                requestBuilder.addHeader("X-Test-Request", "tested-per-request");
                return requestBuilder;
            }
        });
        response = builder.post(text("echo"));
        assertEquals("POSTed echo", response.readEntity(String.class));
        assertEquals("tested", response.getHeaderString("X-Test-Config"));
        assertEquals("tested-per-request", response.getHeaderString("X-Test-Request"));


        // now using global request customizer again
        response = target("test").request().post(text("echo"));
        assertEquals("POSTed echo", response.readEntity(String.class));
        assertEquals("tested", response.getHeaderString("X-Test-Config"));
        assertEquals("tested-global", response.getHeaderString("X-Test-Request"));
    }
}
