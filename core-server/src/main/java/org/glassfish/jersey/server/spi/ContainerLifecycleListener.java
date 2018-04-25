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

package org.glassfish.jersey.server.spi;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.spi.Contract;

/**
 * Classes implementing this contract receive container life-cycle notification
 * events.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ContainerLifecycleListener {

    /**
     * Invoked at the {@link Container container} start-up. This method is invoked even
     * when application is reloaded and new instance of application has started.
     *
     * @param container container that has been started.
     */
    public void onStartup(Container container);

    /**
     * Invoked when the {@link Container container} has been reloaded.
     *
     * @param container container that has been reloaded.
     */
    public void onReload(Container container);

    /**
     * Invoke at the {@link Container container} shut-down. This method is invoked even before
     * the application is being stopped as a part of reload.
     *
     * @param container container that has been shut down.
     */
    public void onShutdown(Container container);
}
