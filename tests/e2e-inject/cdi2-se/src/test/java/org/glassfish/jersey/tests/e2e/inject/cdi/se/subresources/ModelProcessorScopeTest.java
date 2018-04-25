/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject.cdi.se.subresources;

import java.util.concurrent.ExecutionException;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.tests.e2e.inject.cdi.se.RequestContextBuilder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test scope of resources enhanced by model processors.
 *
 * @author Miroslav Fuksa
 *
 */
public class ModelProcessorScopeTest {

    private void _testCounter(ApplicationHandler applicationHandler, String requestUri, final String prefix,
                              final String expectedSecondHit) throws
            InterruptedException, ExecutionException {
        ContainerResponse response = applicationHandler.apply(RequestContextBuilder.from(requestUri,
                "GET").build()).get();
        assertEquals(200, response.getStatus());
        assertEquals(prefix + ":0", response.getEntity());
        response = applicationHandler.apply(RequestContextBuilder.from(requestUri,
                "GET").build()).get();
        assertEquals(prefix + ":" + expectedSecondHit, response.getEntity());
    }

    @Test
    public void testSingleton() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(ModelProcessorFeature
                .SingletonResource.class));
        final String requestUri = "/singleton";
        _testCounter(applicationHandler, requestUri, "SingletonResource", "1");
    }

    @Test
    public void testSingletonInModelProcessor() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(RootResource.class,
                ModelProcessorFeature.class));
        final String requestUri = "/singleton";
        _testCounter(applicationHandler, requestUri, "SingletonResource", "1");
    }

    @Test
    public void testSubResourceSingletonInOriginalModel() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(RootResource.class));
        final String requestUri = "/root/sub-resource-singleton";
        _testCounter(applicationHandler, requestUri, "SubResourceSingleton", "1");
    }

    @Test
    public void testSubResourceEnhancedSingleton() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(RootResource.class));
        final String requestUri = "/root/sub-resource-singleton/enhanced-singleton";
        _testCounter(applicationHandler, requestUri, "EnhancedSubResourceSingleton", "1");
    }

    @Test
    public void testSubResourceInstanceEnhancedSingleton() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(RootResource.class));
        final String requestUri = "/root/sub-resource-instance/enhanced-singleton";
        _testCounter(applicationHandler, requestUri, "EnhancedSubResourceSingleton", "1");
    }

    @Test
    public void testSubResourceInstanceEnhancedSubResource() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(RootResource.class));
        final String requestUri = "/root/sub-resource-instance/enhanced";
        _testCounter(applicationHandler, requestUri, "EnhancedSubResource", "0");
    }

    @Test
    public void testSubResourceEnhancedSubResource() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(RootResource.class));
        final String requestUri = "/root/sub-resource-singleton/enhanced";
        _testCounter(applicationHandler, requestUri, "EnhancedSubResource", "0");
    }

    @Test
    public void testInstanceInModelProcessor() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(RootResource.class,
                ModelProcessorFeature.class));
        final String requestUri = "/instance";
        _testCounter(applicationHandler, requestUri, "Inflector", "1");
    }

    @Test
    public void testRootSingleton() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(RootResource.class,
                RootSingletonResource.class));
        final String requestUri = "/root-singleton";
        _testCounter(applicationHandler, requestUri, "RootSingletonResource", "1");
    }

    @Test
    public void testRequestScopeResource() throws ExecutionException, InterruptedException {
        ApplicationHandler applicationHandler = new ApplicationHandler(new ResourceConfig(RootResource.class,
                RootSingletonResource.class, ModelProcessorFeature.class));
        final String requestUri = "/request-scope";
        _testCounter(applicationHandler, requestUri, "RequestScopeResource", "0");
    }
}
