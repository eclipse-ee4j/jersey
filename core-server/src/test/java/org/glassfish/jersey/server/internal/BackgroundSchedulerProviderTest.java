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

package org.glassfish.jersey.server.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import javax.inject.Inject;

import org.glassfish.jersey.internal.util.Producer;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.BackgroundScheduler;
import org.glassfish.jersey.spi.ScheduledThreadPoolExecutorProvider;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test basic application behavior.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Michal Gajdos
 */
public class BackgroundSchedulerProviderTest {

    private ApplicationHandler createApplication(Class<?>... classes) {
        final ResourceConfig resourceConfig = new ResourceConfig(classes);

        return new ApplicationHandler(resourceConfig);
    }

    public static final class CustomThread extends Thread {
        public CustomThread(Runnable target) {
            super(target);
        }
    }

    @BackgroundScheduler
    public static final class CustomThreadProvider extends ScheduledThreadPoolExecutorProvider {

        public CustomThreadProvider() {
            super("custom-scheduler");
        }

        @Override
        public ThreadFactory getBackingThreadFactory() {
            return new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new CustomThread(r);
                }
            };
        }
    }

    @Path("executors-test")
    public static final class TestResource {
        @Inject
        @BackgroundScheduler
        private ScheduledExecutorService bs;

        @GET
        public int getTestResult() throws ExecutionException, InterruptedException {
            int result = 1; // method invoked

            final Future<Integer> future = bs.submit(new Producer<Integer>() {
                @Override
                public Integer call() {
                    final Thread thread = Thread.currentThread();
                    if (thread instanceof CustomThread) {
                        return 10; // CustomThreadProvider used to provide BackgroundScheduler executor service
                    }

                    return 0;
                }
            });

            result += future.get();

            return result;
        }
    }

    @Test
    public void testCustomRuntimeThreadProviderSupport() throws ExecutionException, InterruptedException {
        ApplicationHandler ah = createApplication(CustomThreadProvider.class, TestResource.class);

        final ContainerResponse response = ah.apply(RequestContextBuilder.from("/executors-test", "GET").build()).get();

        assertEquals(200, response.getStatus());
        assertEquals("Some executor test assertions failed.", 11, response.getEntity());
    }
}
