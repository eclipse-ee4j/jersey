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

package org.glassfish.jersey.tests.e2e.server;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.NameBinding;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test {@link NameBinding name binding} annotations on the {@link Application} class.
 *
 * @author Miroslav Fuksa
 */
public class AppNameBindingTest2 extends JerseyTest {

    @Override
    protected Application configure() {
        return new MyApp();
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface FirstGlobalNameBinding {
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface SecondGlobalNameBinding {
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface ThirdGlobalNameBinding {
    }

    @Provider
    @FirstGlobalNameBinding
    public static class AddOneInterceptor implements
            WriterInterceptor {
        public void aroundWriteTo(WriterInterceptorContext context)
                throws IOException, WebApplicationException {
            String entity = (String) context.getEntity();
            Integer i = Integer.parseInt(entity);
            entity = String.valueOf(i + 1);
            context.setEntity(entity);
            context.proceed();
        }
    }

    @Provider
    @FirstGlobalNameBinding
    @SecondGlobalNameBinding
    public static class AddHundredInterceptor implements
            WriterInterceptor {
        public void aroundWriteTo(WriterInterceptorContext context)
                throws IOException, WebApplicationException {
            String entity = (String) context.getEntity();
            Integer i = Integer.parseInt(entity);
            entity = String.valueOf(i + 100);
            context.setEntity(entity);
            context.proceed();
        }
    }

    @Provider
    @FirstGlobalNameBinding
    @SecondGlobalNameBinding
    @ThirdGlobalNameBinding
    public static class AddThousandInterceptor implements
            WriterInterceptor {
        public void aroundWriteTo(WriterInterceptorContext context)
                throws IOException, WebApplicationException {
            String entity = (String) context.getEntity();
            Integer i = Integer.parseInt(entity);
            entity = String.valueOf(i + 1000);
            context.setEntity(entity);
            context.proceed();
        }
    }

    @Provider
    @FirstGlobalNameBinding
    public static class AddTenFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext,
                           ContainerResponseContext responseContext) throws IOException {
            String entity = (String) responseContext.getEntity();
            Integer i = Integer.valueOf(entity);
            entity = String.valueOf(i + 10);
            responseContext.setEntity(entity, (Annotation[]) null,
                    MediaType.TEXT_PLAIN_TYPE);
        }
    }

    @FirstGlobalNameBinding
    @SecondGlobalNameBinding
    public class MyApp extends Application {

        public java.util.Set<java.lang.Class<?>> getClasses() {
            Set<Class<?>> resources = new HashSet<Class<?>>();
            resources.add(Resource.class);
            resources.add(AddOneInterceptor.class);
            resources.add(AddTenFilter.class);
            resources.add(AddHundredInterceptor.class);
            resources.add(AddThousandInterceptor.class);
            return resources;
        }
    }

    @Path("resource")
    public static class Resource {

        @POST
        @Path("bind")
        @FirstGlobalNameBinding
        @ThirdGlobalNameBinding
        @Produces("text/plain")
        public String echoWithBind(String echo) {
            // note: AddThousandInterceptor will not be triggered even we have here @ThirdGlobalNameBinding. Annotations from
            // Application class and from resource methods are evaluated separately.

            return echo;
        }

        @POST
        @Path("nobind")
        @Produces("text/plain")
        public String echoNoBind(String echo) {
            return echo;
        }
    }

    @Test
    public void testBind() {
        final Response response = target().path("resource/bind").request(MediaType.TEXT_PLAIN_TYPE).post(
                Entity.entity(Integer.valueOf(0), MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        final Integer integer = response.readEntity(Integer.class);
        assertEquals(111, integer.intValue());
    }


    @Test
    public void testNoBind() {
        final Response response = target().path("resource/nobind").request(MediaType.TEXT_PLAIN_TYPE).post(
                Entity.entity(Integer.valueOf(0), MediaType.TEXT_PLAIN_TYPE));
        assertEquals(200, response.getStatus());
        final Integer integer = response.readEntity(Integer.class);
        assertEquals(111, integer.intValue());
    }

}
