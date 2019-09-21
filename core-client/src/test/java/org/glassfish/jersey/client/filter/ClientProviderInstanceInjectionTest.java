/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.filter;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import javax.inject.Inject;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests injections into provider instances.
 *
 * @author Miroslav Fuksa
 * @author Michal Gajdos
 */
public class ClientProviderInstanceInjectionTest {

    public static class MyInjectee {

        private final String value;

        public MyInjectee(final String value) {
            this.value = value;
        }

        public String getSomething() {
            return value;
        }
    }

    public static class MyInjecteeBinder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(new MyInjectee("hello"));
        }
    }

    public static class MyFilter implements ClientRequestFilter {

        private final Object field;

        @Inject
        private MyInjectee myInjectee;

        public MyFilter(Object field) {
            this.field = field;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.ok(myInjectee + "," + field).build());
        }
    }

    public static class MyFilterFeature implements Feature {

        @Inject
        private InjectionManager injectionManager;

        @Override
        public boolean configure(final FeatureContext context) {
            context.register(new MyFilter(injectionManager));
            return true;
        }
    }

    /**
     * Tests that instance of a feature or other provider will not be injected on the client-side.
     */
    @Test
    public void test() {
        final Client client = ClientBuilder.newBuilder()
                .register(new MyFilterFeature())
                .register(new MyInjecteeBinder())
                .build();
        final Response response = client.target("http://foo.bar").request().get();

        assertEquals(200, response.getStatus());
        assertEquals("null,null", response.readEntity(String.class));
    }
}
