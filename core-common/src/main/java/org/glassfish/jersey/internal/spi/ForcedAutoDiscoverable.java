/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * A service provider contract for JAX-RS and Jersey components that need to be automatically discovered and registered in
 * {@link javax.ws.rs.core.Configuration runtime configurations}.
 * <p/>
 * A component implementing this contract becomes auto-discoverable by adding a new entry with fully qualified name of its
 * implementation class name to a {@code org.glassfish.jersey.internal.spi.AutoDiscoverable} file in the {@code
 * META-INF/services} directory.
 * <p/>
 * Implementations of this contract are always {@link #configure(javax.ws.rs.core.FeatureContext) configured} regardless of
 * values of properties {@link org.glassfish.jersey.CommonProperties#FEATURE_AUTO_DISCOVERY_DISABLE} and
 * {@link org.glassfish.jersey.CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE}.
 *
 * @author Michal Gajdos
 * @see org.glassfish.jersey.internal.spi.AutoDiscoverable
 */
public interface ForcedAutoDiscoverable extends AutoDiscoverable {
}
