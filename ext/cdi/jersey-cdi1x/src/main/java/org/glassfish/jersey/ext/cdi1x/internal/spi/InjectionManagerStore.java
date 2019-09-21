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

package org.glassfish.jersey.ext.cdi1x.internal.spi;

import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * {@link InjectionManager injection manager} designed for Jersey
 * {@link javax.enterprise.inject.spi.Extension CDI extension}. This SPI is designed to support deployments that can contain
 * more than one Jersey/InjectionManager managed CDI {@link org.glassfish.jersey.server.spi.ComponentProvider component provider}
 * (more injection manager) but only single CDI extension instance (e.g. EAR with multiple WARs). Each CDI component provider
 * instance acknowledges the manager about new injection manager and manager is supposed to return the effective injection manager
 * for the current context (based on the Servlet context, for example).
 *
 * @author Michal Gajdos
 * @since 2.17
 */
public interface InjectionManagerStore {

    /**
     * Register a new {@link InjectionManager injection manager} with this manager.
     *
     * @param injectionManager injection manager to be registered.
     */
    public void registerInjectionManager(InjectionManager injectionManager);

    /**
     * Obtain the effective {@link InjectionManager injection manager}. The implementations are supposed to
     * decide which of the registered injection managers is the currently effective locator. The decision can be based, for
     * example, on current Servlet context (if the application is deployed on Servlet container).
     *
     * @return currently effective injection manager.
     */
    public InjectionManager getEffectiveInjectionManager();
}
