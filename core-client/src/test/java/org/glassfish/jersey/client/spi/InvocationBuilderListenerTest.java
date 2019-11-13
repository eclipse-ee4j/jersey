/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.spi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class InvocationBuilderListenerTest {

    private static final String PROPERTY_NAME = "test_property";
    private static final String ONE = "one";

    private WebTarget target;

    @Before
    public void setUp() {
        target = ClientBuilder.newClient().target("http://localhost:8080").register(AbortRequestFilter.class)
                .register(new PropertySetterInvocationBuilderListener(a -> a.property(key(ONE), ONE)));
    }

    @Test
    public void testRequest() throws ExecutionException, InterruptedException {
        try (Response r = target.request().async().get().get()) {
            assertDefault(r);
        }
    }

    @Test
    public void testRequestString() {
        try (Response r = target.request(MediaType.TEXT_HTML).build("GET").invoke()) {
            assertDefault(r);
        }
    }

    @Test
    public void testRequestMediaType() throws ExecutionException, InterruptedException {
        try (Response r = target.request(MediaType.TEXT_PLAIN_TYPE).rx().get().toCompletableFuture().get()) {
            assertDefault(r);
        }
    }

    private void assertDefault(Response response) {
        Assert.assertEquals(key(ONE) + "=" + ONE, response.readEntity(String.class));
    }

    private static String key(String keySuffix) {
        return new StringBuilder().append(PROPERTY_NAME).append('_').append(keySuffix).toString();
    }

    public static class PropertySetterInvocationBuilderListener implements InvocationBuilderListener {

        private final Consumer<InvocationBuilderContext> builderConsumer;

        public PropertySetterInvocationBuilderListener(Consumer<InvocationBuilderContext> builderConsumer) {
            this.builderConsumer = builderConsumer;
        }

        @Override
        public void onNewBuilder(InvocationBuilderContext context) {
            builderConsumer.accept(context);
        }
    }

    public static class AbortRequestFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            StringBuilder sb = new StringBuilder();
            for (String propertyName : requestContext.getPropertyNames()) {
                if (propertyName.startsWith(PROPERTY_NAME)) {
                    sb.append(propertyName).append("=").append(requestContext.getProperty(propertyName));
                }
            }
            requestContext.abortWith(Response.ok().entity(sb.toString()).build());
        }
    }
}
