/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.BackgroundScheduler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.spi.ScheduledThreadPoolExecutorProvider;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ScheduledThreadPoolExecutorProviderTest {

    @Test
    public void restartShutsdownScheduler() throws Exception {
        WebContainer container = new WebContainer();
        container.setUp();
        container.tearDown();
        assertNotNull(CustomScheduledThreadPoolExecutorProvider.executor);
        assertTrue(CustomScheduledThreadPoolExecutorProvider.selfRef.isClosed());
        assertTrue(CustomScheduledThreadPoolExecutorProvider.executor.isShutdown());
    }

    private static class WebContainer extends JerseyTest {
        @Override
        protected Application configure() {
            return new ResourceConfig(CustomScheduledThreadPoolExecutorProvider.class);
        }
    }

    @BackgroundScheduler
    @Singleton
    public static class CustomScheduledThreadPoolExecutorProvider extends ScheduledThreadPoolExecutorProvider {

        private static CustomScheduledThreadPoolExecutorProvider selfRef;
        private static ScheduledThreadPoolExecutor executor;

        public CustomScheduledThreadPoolExecutorProvider() {
            this("CustomScheduledThreadPoolExecutorProvider");
        }

        public CustomScheduledThreadPoolExecutorProvider(String name) {
            super(name);
            CustomScheduledThreadPoolExecutorProvider.selfRef = this;
        }

        @Override
        protected ScheduledThreadPoolExecutor createExecutor(int corePoolSize, ThreadFactory threadFactory,
                RejectedExecutionHandler handler) {
            executor = new ScheduledThreadPoolExecutor(5, threadFactory, handler);
            return executor;
        }
    }
}
