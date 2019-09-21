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

package org.glassfish.jersey.internal.spi;

import javax.ws.rs.core.FeatureContext;

/**
 * A service provider contract for JAX-RS and Jersey components that need to be automatically discovered and registered in
 * {@link javax.ws.rs.core.Configuration runtime configurations}.
 * <p/>
 * A component implementing this contract becomes auto-discoverable by adding a new entry with fully qualified name of its
 * implementation class name to a {@code org.glassfish.jersey.internal.spi.AutoDiscoverable} file in the {@code
 * META-INF/services} directory.
 * <p/>
 * Almost all Jersey {@code AutoDiscoverable} implementations have
 * {@link #DEFAULT_PRIORITY} {@link javax.annotation.Priority priority} set.
 *
 * @author Michal Gajdos
 */
public interface AutoDiscoverable {

    /**
     * Default common priority of Jersey build-in auto-discoverables.
     * Use lower number on your {@code AutoDiscoverable} implementation to run it before Jersey auto-discoverables
     * and vice versa.
     */
    public static final int DEFAULT_PRIORITY = 2000;

    /**
     * A call-back method called when an auto-discoverable component is to be configured in a given runtime configuration scope.
     * <p>
     * Note that as with {@link javax.ws.rs.core.Feature JAX-RS features}, before registering new JAX-RS components in a
     * given configurable context, an auto-discoverable component should verify that newly registered components are not
     * already registered in the configurable context.
     * </p>
     *
     * @param context configurable context in which the auto-discoverable should be configured.
     */
    public void configure(FeatureContext context);
}
