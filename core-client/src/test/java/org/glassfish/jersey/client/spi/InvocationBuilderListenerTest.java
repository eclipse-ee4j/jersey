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

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
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

    @Test
    public void testConfigurationProperties() {
        String value = "OTHER_VALUE";
        try (Response r = target.property(key(ConfigurationInvocationBuilderListener.OTHER_PROPERTY), value)
                .register(ConfigurationInvocationBuilderListener.class).request().get()) {
            Assert.assertTrue(
                    r.readEntity(String.class).contains(key(ConfigurationInvocationBuilderListener.OTHER_PROPERTY) + "=" + value)
            );
        }
    }

    @Test
    public void testGetters() {
        try (Response r = target.register(SetterInvocationBuilderListener.class, 100)
                .register(GetterInvocationBuilderListener.class, 200).request().get()) {
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

    public static class ConfigurationInvocationBuilderListener implements InvocationBuilderListener {
        static final String OTHER_PROPERTY = "OTHER_PROPERTY";

        @Override
        public void onNewBuilder(InvocationBuilderContext context) {
            context.property(key(OTHER_PROPERTY), context.getConfiguration().getProperty(key(OTHER_PROPERTY)));
        }
    }

    public static class SetterInvocationBuilderListener implements InvocationBuilderListener {

        @Override
        public void onNewBuilder(InvocationBuilderContext context) {
            context.accept(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON_PATCH_JSON_TYPE)
                    .acceptEncoding("GZIP")
                    .acceptLanguage(Locale.GERMAN)
                    .acceptLanguage(new Locale.Builder().setLanguage("sr").setScript("Latn").setRegion("RS").build())
                    .property(PROPERTY_NAME, PROPERTY_NAME)
                    .cacheControl(CacheControl.valueOf(PROPERTY_NAME))
                    .cookie("Cookie", "CookieValue")
                    .header(HttpHeaders.CONTENT_ID, PROPERTY_NAME);
        }
    }

    public static class GetterInvocationBuilderListener implements InvocationBuilderListener {

        @Override
        public void onNewBuilder(InvocationBuilderContext context) {
            Date date = new Date();
            RuntimeDelegate.HeaderDelegate localeDelegate = RuntimeDelegate.getInstance().createHeaderDelegate(Locale.class);
            Assert.assertThat(context.getAccepted(),
                    Matchers.containsInAnyOrder(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_PATCH_JSON));
            Assert.assertThat(context.getEncodings(), Matchers.contains("GZIP"));
            Assert.assertThat(context.getAcceptedLanguages(),
                    Matchers.containsInAnyOrder(localeDelegate.toString(Locale.GERMAN),
                            localeDelegate.toString(
                                    new Locale.Builder().setLanguage("sr").setScript("Latn").setRegion("RS").build()
                            )
                    )
            );

            Assert.assertThat(context.getHeader(HttpHeaders.CONTENT_ID), Matchers.contains(PROPERTY_NAME));
            context.getHeaders().add(HttpHeaders.DATE, date);
            Assert.assertThat(context.getHeader(HttpHeaders.DATE), Matchers.notNullValue());
            Assert.assertThat(context.getHeaders().getFirst(HttpHeaders.DATE), Matchers.is(date));

            Assert.assertNotNull(context.getUri());
            Assert.assertTrue(context.getUri().toASCIIString().startsWith("http://"));

            Assert.assertThat(context.getPropertyNames(), Matchers.contains(PROPERTY_NAME));
            Assert.assertThat(context.getProperty(PROPERTY_NAME), Matchers.is(PROPERTY_NAME));
            context.removeProperty(PROPERTY_NAME);
            Assert.assertTrue(context.getPropertyNames().isEmpty());

            Assert.assertThat(context.getCacheControls().get(0).toString(),
                    Matchers.is(CacheControl.valueOf(PROPERTY_NAME).toString())
            );
            Assert.assertThat(context.getCookies().size(), Matchers.is(1));
            Assert.assertThat(context.getCookies().get("Cookie"), Matchers.notNullValue());
        }
    }
}
