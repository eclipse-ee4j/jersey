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

import javax.ejb.Singleton;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

/**
 * Singleton EJB bean that is used to reload the first web application.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Singleton
public class EjbReloaderService {

    Container container;

    public void reload() {
        container.reload(newApp());
    }

    /**
     * Create new resource config that contains {@link ReloadDetectionResource} singleton
     * with current nano time, so that we can detect when the application has been initialized.
     *
     * @return new resource config.
     */
    private ResourceConfig newApp() {

        ResourceConfig result = new ResourceConfig();
        result.register(ReloadDetectionResource.createNewInstance());
        result.register(ContainerListener.class);
        return result;
    }

    /**
     * Set the container to be reloaded. Invoked from {@link ContainerListener}.
     *
     * @param container to be reloaded.
     */
    public void setContainer(Container container) {
        this.container = container;
    }
}
