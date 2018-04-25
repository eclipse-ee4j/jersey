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

package org.glassfish.jersey.tests.integration.servlet_3_init_provider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

/**
 * This is just test purpose implementation of Jersey SPI {@link ContainerLifecycleListener}.
 * The listener class is registered in {@link TestServletContainerProvider} to {@link ResourceConfig}.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class TestContainerLifecycleListener implements ContainerLifecycleListener {

    private static int startupCount = 0;

    private static CountDownLatch latch = new CountDownLatch(AbstractHelloWorldResource.NUMBER_OF_APPLICATIONS);

    @Override
    public void onStartup(Container container) {
        latch.countDown();
        startupCount++;
    }

    @Override
    public void onReload(Container container) {
    }

    @Override
    public void onShutdown(Container container) {
    }

    public static int getStartupCount() throws InterruptedException {
        latch.await(5, TimeUnit.SECONDS);
        return startupCount;
    }

}
