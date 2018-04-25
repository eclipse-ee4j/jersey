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

package org.glassfish.jersey.tests.e2e.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.AnnotationLiteral;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class CustomInjectablesResourceConfigTest extends JerseyTest {

    public static class MyHK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            // request scope binding
            bindAsContract(MyInjectablePerRequest.class).in(RequestScoped.class);

            // singleton binding
            bindAsContract(MyInjectableSingleton.class).in(Singleton.class);

            // singleton instance binding
            bind(new MyInjectableSingleton()).to(MyInjectableSingleton.class);

            // request scope binding with specified custom annotation
            bindAsContract(MyInjectablePerRequest.class).qualifiedBy(new MyQualifierImpl()).in(RequestScoped.class);
        }
    }

    public static class MyInjectablePerRequest {
        public int i = 0;
    }

    @Singleton
    public static class MyInjectableSingleton {
        public int i = 0;
    }

    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Qualifier
    public static @interface MyQualifier {

    }

    private static class MyQualifierImpl extends AnnotationLiteral<MyQualifier> implements MyQualifier {
    }

    @Path("/")
    public static class Resource {
        @Inject
        MyInjectablePerRequest myInjectablePerRequest;

        @Inject
        MyInjectableSingleton myInjectableSingleton;

        @Inject
        @MyQualifier
        MyInjectablePerRequest myInjectablePerRequest2;

        @GET
        @Path("/perrequest")
        public String getAndIncPerRequest() {
            return Integer.valueOf(++myInjectablePerRequest.i).toString();
        }

        @GET
        @Path("/perrequestCustomQualifier")
        public String getAndIncPerRequest2() {
            return Integer.valueOf(++myInjectablePerRequest2.i).toString();
        }

        @GET
        @Path("/singleton")
        @Produces("text/plain")
        public String getAndIncSingleton() {
            System.out.println(myInjectableSingleton);
            return Integer.valueOf(++myInjectableSingleton.i).toString();
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig rc = new ResourceConfig();
        rc.registerClasses(Resource.class);
        rc.register(new MyHK2Binder());

        return rc;
    }

    @Test
    public void testPerRequest() throws Exception {
        final javax.ws.rs.client.WebTarget perRequest = target().path("perrequest");

        assertEquals("1", perRequest.request().get(String.class));
        assertEquals("1", perRequest.request().get(String.class));
        assertEquals("1", perRequest.request().get(String.class));
    }

    @Test
    public void testSingleton() throws Exception {
        final javax.ws.rs.client.WebTarget perRequest = target().path("singleton");

        assertEquals("1", perRequest.request().get(String.class));
        assertEquals("2", perRequest.request().get(String.class));
        assertEquals("3", perRequest.request().get(String.class));
    }

    @Test
    public void testCustomAnnotation() throws Exception {
        final javax.ws.rs.client.WebTarget perRequestCustomAnnotation = target().path("perrequestCustomQualifier");

        assertEquals("1", perRequestCustomAnnotation.request().get(String.class));
        assertEquals("1", perRequestCustomAnnotation.request().get(String.class));
        assertEquals("1", perRequestCustomAnnotation.request().get(String.class));
    }
}
