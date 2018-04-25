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

package org.glassfish.jersey.server.spring;

import javax.ws.rs.ext.Provider;

import javax.inject.Inject;

import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * JAX-RS Provider class for processing Jersey 2 Spring integration container life-cycle events.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
@Provider
public class SpringLifecycleListener implements ContainerLifecycleListener {

    @Inject
    private ApplicationContext ctx;

    @Override
    public void onStartup(Container container) {
    }

    @Override
    public void onReload(Container container) {
        if (ctx instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) ctx).refresh();
        }
    }

    @Override
    public void onShutdown(Container container) {
        if (ctx instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) ctx).close();
        }
    }
}
