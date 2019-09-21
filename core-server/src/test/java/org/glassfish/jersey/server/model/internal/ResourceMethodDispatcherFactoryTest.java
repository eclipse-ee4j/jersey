/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.TestInjectionManagerFactory;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModelComponent;
import org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jakub Podlesak
 */
public class ResourceMethodDispatcherFactoryTest {

    private ResourceMethodDispatcherFactory rmdf;
    private ResourceMethodInvocationHandlerFactory rmihf;

    @Before
    public void setupApplication() {
        TestInjectionManagerFactory.BootstrapResult result = TestInjectionManagerFactory.createInjectionManager();

        List<ResourceMethodDispatcher.Provider> providers = Arrays.asList(
                new VoidVoidDispatcherProvider(result.bootstrapBag.getResourceContext()),
                new JavaResourceMethodDispatcherProvider(result.bootstrapBag.getValueParamProviders()));

        rmdf = new ResourceMethodDispatcherFactory(providers);
        rmihf = new ResourceMethodInvocationHandlerFactory(result.injectionManager);
    }

    @Test
    public void testBasicDispatchers() throws InterruptedException, ExecutionException {
        final Resource.Builder rb = Resource.builder();

        final Method[] methods = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPrivate(method.getModifiers())) {
                // class-based
                rb.addMethod("GET").handledBy(this.getClass(), method);
                // instance-based
                rb.addMethod("GET").handledBy(this, method);
            }
        }

        for (ResourceModelComponent component : rb.build().getComponents()) {
            if (component instanceof ResourceMethod) {
                Invocable invocable = ((ResourceMethod) component).getInvocable();
                assertNotNull("No dispatcher found for invocable " + invocable.toString(),
                        rmdf.create(invocable, rmihf.create(invocable), null));
            }
        }

    }

    private void voidVoid() {
        // do nothing
    }

    private String voidString() {
        // do nothing
        return null;
    }

    private Response voidResponse() {
        // do nothing
        return null;
    }

    private void stringVoid(String s) {
        // do nothing
    }

    private String stringString(String s) {
        // do nothing
        return null;
    }

    private void requestVoid(Request s) {
        // do nothing
    }

    private String requestString(Request s) {
        // do nothing
        return null;
    }

    private Response requestResponse(Request s) {
        // do nothing
        return null;
    }
}
