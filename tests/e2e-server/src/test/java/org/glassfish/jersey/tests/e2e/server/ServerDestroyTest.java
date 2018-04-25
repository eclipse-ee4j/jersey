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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Assert that pre destroy method on application, resources and providers is invoked.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos
 */
public class ServerDestroyTest extends JerseyTest {

    private static final Map<String, Boolean> destroyed = new HashMap<>();

    private Reloader reloader;

    @Override
    @Before
    public void setUp() throws Exception {
        destroyed.clear();
        destroyed.put("application", false);
        destroyed.put("singleton-resource", false);
        destroyed.put("filter", false);
        destroyed.put("writer", false);
        destroyed.put("singleton-factory", false);
        destroyed.put("feature", false);

        super.setUp();
    }

    @Path("/")
    @Singleton
    public static class Resource {

        @GET
        public String get() {
            return "resource";
        }

        @PreDestroy
        public void preDestroy() {
            destroyed.put("singleton-resource", true);
        }
    }

    public static class MyFilter implements ContainerResponseFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext,
                           final ContainerResponseContext responseContext) throws IOException {
            responseContext.getHeaders().putSingle("foo", "bar");
        }

        @PreDestroy
        public void preDestroy() {
            destroyed.put("filter", true);
        }
    }

    public static class MyWriter implements WriterInterceptor {

        @Override
        public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
            context.setEntity("writer-" + context.getEntity());
            context.proceed();
        }

        @PreDestroy
        public void preDestroy() {
            destroyed.put("writer", true);
        }
    }

    public static class MyApplication extends Application {

        @PreDestroy
        public void preDestroy() {
            destroyed.put("application", true);
        }

        @Override
        public Set<Class<?>> getClasses() {
            return Arrays.asList(
                    Resource.class,
                    MyFilter.class,
                    MyWriter.class,
                    MyContainerLifecycleListener.class,
                    MyFeature.class).stream().collect(Collectors.toSet());
        }

        @Override
        public Set<Object> getSingletons() {
            return Collections.singleton(new AbstractBinder() {
                @Override
                protected void configure() {
                    bindFactory(SingletonFactory.class)
                            .to(SingletonInstance.class)
                            .in(Singleton.class);
                }
            });
        }
    }

    public static class SingletonInstance {

        public void dispose() {
            destroyed.put("singleton-factory", true);
        }
    }

    public static class SingletonFactory implements DisposableSupplier<SingletonInstance> {

        @Override
        public SingletonInstance get() {
            return new SingletonInstance();
        }

        @Override
        public void dispose(final SingletonInstance instance) {
            instance.dispose();
        }
    }

    private static class Reloader extends AbstractContainerLifecycleListener {

        Container container;

        public void reload(final ResourceConfig config) {
            container.reload(config);
        }

        @Override
        public void onStartup(final Container container) {
            this.container = container;
        }
    }

    public static class MyContainerLifecycleListener extends AbstractContainerLifecycleListener {

        @Inject
        private SingletonInstance instance;

        @Override
        public void onShutdown(final Container container) {
            assertThat(instance, notNullValue());
        }
    }

    public static class MyFeature implements Feature {

        @PreDestroy
        public void preDestroy() {
            destroyed.put("feature", true);
        }

        @Override
        public boolean configure(final FeatureContext context) {
            return true;
        }
    }

    @Override
    protected DeploymentContext configureDeployment() {
        reloader = new Reloader();

        return DeploymentContext.newInstance(ResourceConfig.forApplicationClass(MyApplication.class).register(reloader));
    }

    @Test
    public void testApplicationResource() throws Exception {
        final Response response = target().request().get();
        assertThat(response.readEntity(String.class), is("writer-resource"));
        assertThat(response.getStringHeaders().getFirst("foo"), is("bar"));

        checkDestroyed(false);
        reloader.reload(new ResourceConfig(Resource.class));
        checkDestroyed(true);
    }

    private void checkDestroyed(final boolean shouldBeDestroyed) {
        for (final Map.Entry<String, Boolean> entry : destroyed.entrySet()) {
            assertThat(entry.getKey() +  " should" + (shouldBeDestroyed ? "" : " not") + " be destroyed",
                    entry.getValue(), is(shouldBeDestroyed));
        }
    }
}
