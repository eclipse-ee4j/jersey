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

package org.glassfish.jersey.tests.e2e.server;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
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
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link InjectionManagerProvider}.
 *
 * @author Miroslav Fuksa
 */
public class InjectionManagerServerProviderTest extends JerseyTest {

    @Path("resource")
    public static class TestResource {
        @POST
        @Path("feature")
        @FeatureBound
        public String echoFeature(String entity) {
            return entity;
        }


        @POST
        @Path("reader-interceptor")
        @ReaderInterceptorBound
        public String echoReaderInterceptor(String entity) {
            return entity;
        }

        @POST
        @Path("writer-interceptor")
        @WriterInterceptorBound
        public String echoWriterInterceptor(String entity) {
            return entity;
        }
    }

    @Override
    protected Application configure() {
        final ResourceConfig resourceConfig = new ResourceConfig(TestResource.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(new MyInjectedService("hello")).to(MyInjectedService.class);
            }
        });
        resourceConfig.register(new MyFeature());
        resourceConfig.register(new MyReaderInterceptor());
        resourceConfig.register(new MyWriterInterceptor());
        return resourceConfig;
    }


    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface FeatureBound {
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface ReaderInterceptorBound {
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface WriterInterceptorBound {
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


    public static class MyFeature implements Feature {

        @Override
        public boolean configure(FeatureContext context) {
            context.register(MyFeatureInterceptor.class);
            return true;
        }


        @FeatureBound
        public static class MyFeatureInterceptor implements WriterInterceptor {
            private final String name;

            @Inject
            public MyFeatureInterceptor(MyInjectedService injectedService) {
                this.name = injectedService.getName();
            }

            @Override
            public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
                context.setEntity(((String) context.getEntity()) + "-interceptorfeature-" + name);
                context.proceed();
            }
        }
    }

    @Test
    public void testFeature() {
        final Response response = target().path("resource/feature")
                .request().post(Entity.entity("will-be-extended-by", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("will-be-extended-by-interceptorfeature-hello", response.readEntity(String.class));
    }

    @WriterInterceptorBound
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
    public void testWriterInterceptor() {
        final Response response = target().path("resource/writer-interceptor")
                .request().post(Entity.entity("will-be-extended-by", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("will-be-extended-by-writer-interceptor-hello", response.readEntity(String.class));
    }


    @ReaderInterceptorBound
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
        final Response response = target().path("resource/reader-interceptor")
                .request().post(Entity.entity("will-be-extended-by", MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        assertEquals("will-be-extended-by-reader-interceptor-hello", response.readEntity(String.class));
    }

}
