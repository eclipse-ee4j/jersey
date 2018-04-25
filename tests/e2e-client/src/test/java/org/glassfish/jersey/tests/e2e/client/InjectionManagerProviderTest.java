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

package org.glassfish.jersey.tests.e2e.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.inject.Inject;

import org.glassfish.jersey.InjectionManagerProvider;
import org.glassfish.jersey.client.InjectionManagerClientProvider;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link InjectionManagerClientProvider}.
 *
 * @author Miroslav Fuksa
 */
public class InjectionManagerProviderTest extends JerseyTest {

    @Path("resource")
    public static class TestResource {
        @POST
        public String echo(String entity) {
            return entity;
        }
    }


    @Override
    protected Application configure() {
        return new ResourceConfig(TestResource.class);
    }


    public static class MyInjectedService {
        public final String name;

        public MyInjectedService(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class BinderFeature implements Feature {

        public final String name;

        public BinderFeature(String name) {
            this.name = name;
        }

        @Override
        public boolean configure(FeatureContext context) {
            context.register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(new MyInjectedService(name)).to(MyInjectedService.class);
                }
            });
            return true;
        }
    }


    public static class MyRequestFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            final InjectionManager injectionManager = InjectionManagerClientProvider.getInjectionManager(requestContext);
            final MyInjectedService service = injectionManager.getInstance(MyInjectedService.class);
            final String name = service.getName();
            requestContext.setEntity(name);
        }
    }


    @Test
    public void testRequestFilterInstance() {
        final Response response = target().path("resource")
                .register(new BinderFeature("hello"))
                .register(new MyRequestFilter())
                .request().post(Entity.entity("will-be-overwritten", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("hello", response.readEntity(String.class));
    }

    @Test
    public void testRequestFilterClass() {
        final Response response = target().path("resource")
                .register(new BinderFeature("hello"))
                .register(MyRequestFilter.class)
                .request().post(Entity.entity("will-be-overwritten", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("hello", response.readEntity(String.class));
    }


    public static class MyResponseFilter implements ClientResponseFilter {

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

            final InjectionManager locator = InjectionManagerClientProvider.getInjectionManager(responseContext);
            final MyInjectedService service = locator.getInstance(MyInjectedService.class);
            final String name = service.getName();
            responseContext.setEntityStream(new ByteArrayInputStream(name.getBytes()));
        }
    }

    @Test
    public void testResponseFilterInstance() {
        final Response response = target().path("resource")
                .register(new BinderFeature("world"))
                .register(new MyResponseFilter())
                .request().post(Entity.entity("will-be-overwritten", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("world", response.readEntity(String.class));
    }

    @Test
    public void testResponseFilterClass() {
        final Response response = target().path("resource")
                .register(new BinderFeature("world"))
                .register(MyResponseFilter.class)
                .request().post(Entity.entity("will-be-overwritten", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("world", response.readEntity(String.class));
    }

    public static class MyFeature implements Feature {

        @Override
        public boolean configure(FeatureContext context) {
            context.register(MyFeatureInterceptor.class);
            return true;
        }

        public static class MyFeatureInterceptor implements WriterInterceptor {
            private final String name;

            @Inject
            public MyFeatureInterceptor(MyInjectedService injectedService) {
                this.name = injectedService.getName();
            }

            @Override
            public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
                context.setEntity(context.getEntity() + "-interceptor-" + name);
                context.proceed();
            }
        }
    }

    @Test
    public void testFeatureInstance() {
        final Response response = target().path("resource")
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(new MyInjectedService("feature")).to(MyInjectedService.class);
                    }
                })
                .register(new MyFeature())
                .request().post(Entity.entity("will-be-extended-by", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("will-be-extended-by-interceptor-feature", response.readEntity(String.class));
    }

    @Test
    public void testFeatureClass() {
        final Response response = target().path("resource")
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(new MyInjectedService("feature")).to(MyInjectedService.class);
                    }
                })
                .register(MyFeature.class)
                .request().post(Entity.entity("will-be-extended-by", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("will-be-extended-by-interceptor-feature", response.readEntity(String.class));
    }


    public static class MyWriterInterceptor implements WriterInterceptor {

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            final InjectionManager serviceLocator = InjectionManagerProvider.getInjectionManager(context);
            final MyInjectedService service = serviceLocator.getInstance(MyInjectedService.class);
            context.setEntity(((String) context.getEntity()) + "-writer-interceptor-" + service.getName());
            context.proceed();
        }
    }

    @Test
    public void testWriterInterceptorInstance() {
        final Response response = target().path("resource")
                .register(new BinderFeature("universe"))
                .register(new MyWriterInterceptor())
                .request().post(Entity.entity("will-be-extended-by", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("will-be-extended-by-writer-interceptor-universe", response.readEntity(String.class));
    }

    @Test
    public void testWriterInterceptorClass() {
        final Response response = target().path("resource")
                .register(new BinderFeature("universe"))
                .register(MyWriterInterceptor.class)
                .request().post(Entity.entity("will-be-extended-by", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("will-be-extended-by-writer-interceptor-universe", response.readEntity(String.class));
    }


    public static class MyReaderInterceptor implements ReaderInterceptor {

        @Override
        public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
            final Object entity = context.proceed();
            if (!(entity instanceof String)) {
                return entity;
            }
            final String stringEntity = (String) entity;
            final InjectionManager serviceLocator = InjectionManagerProvider.getInjectionManager(context);
            final MyInjectedService service = serviceLocator.getInstance(MyInjectedService.class);
            return stringEntity + "-reader-interceptor-" + service.getName();
        }
    }


    @Test
    public void testReaderInterceptorInstance() {
        final Response response = target().path("resource")
                .register(new BinderFeature("universe"))
                .register(new MyReaderInterceptor())
                .request().post(Entity.entity("will-be-extended-by", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("will-be-extended-by-reader-interceptor-universe", response.readEntity(String.class));
    }

    @Test
    public void testReaderInterceptorClass() {
        final Response response = target().path("resource")
                .register(new BinderFeature("universe"))
                .register(MyReaderInterceptor.class)
                .request().post(Entity.entity("will-be-extended-by", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("will-be-extended-by-reader-interceptor-universe", response.readEntity(String.class));
    }
}
