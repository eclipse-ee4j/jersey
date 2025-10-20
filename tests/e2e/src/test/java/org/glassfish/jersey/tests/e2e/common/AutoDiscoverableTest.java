/*
 * Copyright (c) 2013, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.AutoDiscoverableConfigurator;
import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.model.internal.CommonConfig;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Note: Auto-discoverables from this test "affects" all other tests in suit.
 *
 * @author Michal Gajdos
 */
public class AutoDiscoverableTest extends JerseyTest {

    private static final String PROPERTY = "AutoDiscoverableTest";

    public static class CommonAutoDiscoverable implements AutoDiscoverable {

        @Override
        public void configure(final FeatureContext context) {
            // Return if PROPERTY is not true - applicable for other tests.
            if (!PropertiesHelper.isProperty(context.getConfiguration().getProperty(PROPERTY))) {
                return;
            }

            context.register(new WriterInterceptor() {
                @Override
                public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
                    context.setEntity(context.getEntity() + "-common");

                    context.proceed();
                }
            }, 1);
        }
    }

    @ConstrainedTo(RuntimeType.CLIENT)
    public static class ClientAutoDiscoverable implements AutoDiscoverable {

        @Override
        public void configure(final FeatureContext context) {
            // Return if PROPERTY is not true - applicable for other tests.
            if (!PropertiesHelper.isProperty(context.getConfiguration().getProperty(PROPERTY))) {
                return;
            }

            context.register(new WriterInterceptor() {
                @Override
                public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
                    context.setEntity(context.getEntity() + "-client");

                    context.proceed();
                }
            }, 10);
        }
    }

    @ConstrainedTo(RuntimeType.SERVER)
    public static class ServerAutoDiscoverable implements AutoDiscoverable {

        @Override
        public void configure(final FeatureContext context) {
            // Return if PROPERTY is not true - applicable for other tests.
            if (!PropertiesHelper.isProperty(context.getConfiguration().getProperty(PROPERTY))) {
                return;
            }

            context.register(new WriterInterceptor() {
                @Override
                public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
                    context.setEntity(context.getEntity() + "-server");

                    context.proceed();
                }
            }, 10);
        }
    }

    @Path("/")
    public static class Resource {

        @POST
        public String post(final String value) {
            return value;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class).property(PROPERTY, true);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.property(PROPERTY, true);
    }

    @Test
    public void testAutoDiscoverableConstrainedTo() throws Exception {
        final Response response = target().request().post(Entity.text("value"));

        Assertions.assertEquals("value-common-client-common-server", response.readEntity(String.class));
    }

    @Test
    public void testServiceFinderIterator() {
        Class<AutoDiscoverable>[] array =
                ServiceFinder.service(AutoDiscoverable.class).runtimeType(RuntimeType.SERVER).find().toClassArray();
        int size = array.length;

        Assertions.assertTrue(size > 3);

        ServiceFinder<AutoDiscoverable> finder =
                ServiceFinder.service(AutoDiscoverable.class).runtimeType(RuntimeType.SERVER).find();
        AutoDiscoverable next = null;
        // check next()
        final Iterator<AutoDiscoverable> it = finder.iterator();
        for (int i = 0; i != size; i++) {
            AutoDiscoverable n = it.next();
            Assertions.assertNotSame(next, n);
            next = n;
        }
        Assertions.assertThrows(NoSuchElementException.class, it::next);

        // check hasNext();
        final Iterator<AutoDiscoverable> it2 = finder.iterator();
        next = null;
        for (int i = 0; i != size; i++) {
            for (int j = 0; j != size + 1; j++) {
                Assertions.assertTrue(it2.hasNext());
            }
            AutoDiscoverable n = it2.next();
            Assertions.assertNotSame(next, n);
            next = n;
        }
        Assertions.assertFalse(it2.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it2::next);
    }

    @Test
    public void testAutoDiscoverableConstrainedConfigurator() {
        Class<?>[] array = ServiceFinder.find(AutoDiscoverable.class).toClassArray();
        Assertions.assertTrue(contains(ClientAutoDiscoverable.class, array));
        Assertions.assertTrue(contains(CommonAutoDiscoverable.class, array));
        Assertions.assertTrue(contains(ServerAutoDiscoverable.class, array));

        array = ServiceFinder.service(AutoDiscoverable.class).find().toClassArray();
        Assertions.assertTrue(contains(ClientAutoDiscoverable.class, array));
        Assertions.assertTrue(contains(CommonAutoDiscoverable.class, array));
        Assertions.assertTrue(contains(ServerAutoDiscoverable.class, array));

        array = ServiceFinder.service(AutoDiscoverable.class).runtimeType(RuntimeType.SERVER).find().toClassArray();
        Assertions.assertFalse(contains(ClientAutoDiscoverable.class, array));
        Assertions.assertTrue(contains(CommonAutoDiscoverable.class, array));
        Assertions.assertTrue(contains(ServerAutoDiscoverable.class, array));

        array = ServiceFinder.service(AutoDiscoverable.class).runtimeType(RuntimeType.CLIENT).find().toClassArray();
        Assertions.assertTrue(contains(ClientAutoDiscoverable.class, array));
        Assertions.assertTrue(contains(CommonAutoDiscoverable.class, array));
        Assertions.assertFalse(contains(ServerAutoDiscoverable.class, array));

        AutoDiscoverableConfigurator configurator = new AutoDiscoverableConfigurator(RuntimeType.SERVER);
        InjectionManager injectionManager = Injections.createInjectionManager();
        BootstrapBag bb = new BootstrapBag();
        bb.setConfiguration(new CommonConfig(RuntimeType.SERVER, ComponentBag.INCLUDE_ALL));
        configurator.init(injectionManager, bb);
        array = bb.getAutoDiscoverables().stream().map(ad -> ad.getClass()).collect(Collectors.toList())
                .toArray(new Class[0]);
        Assertions.assertFalse(contains(ClientAutoDiscoverable.class, array));
        Assertions.assertTrue(contains(CommonAutoDiscoverable.class, array));
        Assertions.assertTrue(contains(ServerAutoDiscoverable.class, array));
    }

    private static boolean contains(Class<?> clazz, Class<?>... list) {
        for (Class<?> listClass : list) {
            if (listClass.equals(clazz)) {
                return true;
            }
        }
        return false;
    }
}
