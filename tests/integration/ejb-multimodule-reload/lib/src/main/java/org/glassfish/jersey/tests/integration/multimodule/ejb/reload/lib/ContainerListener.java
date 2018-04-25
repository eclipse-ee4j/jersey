/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.multimodule.ejb.reload.lib;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;

/**
 * JAX-RS resource that keeps number of reloads.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Provider
@Singleton
public class ContainerListener extends AbstractContainerLifecycleListener {

    @EJB EjbReloaderService reloader;

    @Override
    public void onStartup(final Container container) {
        reloader.setContainer(container);
    }
}
