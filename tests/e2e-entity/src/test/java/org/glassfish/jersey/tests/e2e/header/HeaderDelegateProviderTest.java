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

package org.glassfish.jersey.tests.e2e.header;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.message.internal.HeaderUtils;
import org.glassfish.jersey.spi.HeaderDelegateProvider;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HeaderDelegateProviderTest {
    static final String HEADER_NAME = "BEAN_HEADER";
    static final String DISABLED_VALUE = new BeanForHeaderDelegateProviderTest().toString();

    public static class BeanHeaderDelegateProvider implements HeaderDelegateProvider<BeanForHeaderDelegateProviderTest> {

        @Override
        public boolean supports(Class type) {
            return BeanForHeaderDelegateProviderTest.class == type;
        }

        @Override
        public BeanForHeaderDelegateProviderTest fromString(String value) {
            return new BeanForHeaderDelegateProviderTest();
        }

        @Override
        public String toString(BeanForHeaderDelegateProviderTest value) {
            return value.getValue();
        }
    }

    public static class BeanForHeaderDelegateProviderTest {
        public static String getValue() {
            return "CORRECT_VALUE";
        }

        @Override
        public String toString() {
            return "INVALID_VALUE";
        }
    }

    @Path("/")
    public static final class HeaderSettingResource {
        @GET
        @Path("/simple")
        public Response simple() {
            return Response.ok().header(HEADER_NAME, new BeanForHeaderDelegateProviderTest()).build();
        }

        @GET
        @Path("/headers")
        public Response headers(@Context HttpHeaders headers) {
            return Response.ok()
                    .header(HeaderSettingResource.class.getSimpleName(), headers.getHeaderString(HEADER_NAME)).build();
        }

        @GET
        @Path("/clientfilter")
        public Response clientFilterTest(@Context HttpHeaders headers) {
            return Response.ok(headers.getHeaderString(HeaderClientRequestFilter.class.getSimpleName())).build();
        }
    }

    public static final class HeaderContainerResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            String value = responseContext.getHeaderString(HEADER_NAME);
            responseContext.getHeaders().putSingle(HeaderContainerResponseFilter.class.getSimpleName(), value);
        }
    }

    public static final class HeaderClientRequestFilter implements ClientRequestFilter {
        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().putSingle(
                    HeaderClientRequestFilter.class.getSimpleName(), new BeanForHeaderDelegateProviderTest()
            );
        }
    }

    @Test
    public void testTheProviderIsFound() {
        for (HeaderDelegateProvider provider : ServiceFinder.find(HeaderDelegateProvider.class, true)) {
            Assert.assertEquals(provider.getClass(), BeanHeaderDelegateProvider.class);
        }
    }

    @Test
    public void headerDelegateIsUsedWhenRuntimeDelegateDecoratorIsUsed() {
        MultivaluedHashMap headers = new MultivaluedHashMap();
        headers.put(HEADER_NAME, Arrays.asList(new BeanForHeaderDelegateProviderTest()));
        MultivaluedMap<String, String> converted = HeaderUtils.asStringHeaders(headers, null);
        testMap(converted, BeanForHeaderDelegateProviderTest.getValue());

        Client client = ClientBuilder.newClient().property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE, false);
        converted = HeaderUtils.asStringHeaders(headers, client.getConfiguration());
        testMap(converted, BeanForHeaderDelegateProviderTest.getValue());
    }

    @Test
    public void headerDelegateIsNotUsed() {
        MultivaluedHashMap headers = new MultivaluedHashMap();
        headers.put(HEADER_NAME, Arrays.asList(new BeanForHeaderDelegateProviderTest()));

        Client client = ClientBuilder.newClient().property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
        MultivaluedMap<String, String> converted = HeaderUtils.asStringHeaders(headers, client.getConfiguration());
        testMap(converted, DISABLED_VALUE);

        client = ClientBuilder.newClient().property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE_CLIENT, true);
        converted = HeaderUtils.asStringHeaders(headers, client.getConfiguration());
        testMap(converted, DISABLED_VALUE);
    }

    private void testMap(MultivaluedMap<String, String> map, String expectedValue) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            Assert.assertEquals(HEADER_NAME, entry.getKey());
            Assert.assertEquals(expectedValue, entry.getValue().iterator().next());
        }
    }
}
