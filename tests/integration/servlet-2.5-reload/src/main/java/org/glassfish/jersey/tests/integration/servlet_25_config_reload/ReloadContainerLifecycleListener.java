/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_25_config_reload;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;

/**
 * @author Miroslav Fuksa
 */
@Provider
public class ReloadContainerLifecycleListener extends AbstractContainerLifecycleListener {

    private static Container container;

    @Override
    public void onStartup(final Container container) {
        ReloadContainerLifecycleListener.setContainer(container);
    }

    public static Container getContainer() {
        return container;
    }

    public static void setContainer(final Container container) {
        ReloadContainerLifecycleListener.container = container;
    }

}
