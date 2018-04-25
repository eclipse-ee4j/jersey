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

package org.glassfish.jersey.client;

/**
 * Initializable Jersey client-side component.
 * <p>
 * This interface provides method that allows to pre-initialize client-side component's runtime and runtime configuration
 * ahead of it's first use. The interface is implemented by {@link org.glassfish.jersey.client.JerseyClient} and
 * {@link org.glassfish.jersey.client.JerseyWebTarget} classes.
 * </p>
 *
 * @param <T> initializable type.
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @since 2.8
 */
public interface Initializable<T extends Initializable<T>> {

    /**
     * Pre-initializes the runtime and runtime {@link javax.ws.rs.core.Configuration configuration} of this component
     * in order to improve performance during the first request.
     * <p>
     * Once this method is called no other method implementing {@link javax.ws.rs.core.Configurable} should be called
     * on this pre initialized component, otherwise the initialized client runtime will be discarded and the configuration
     * will change back to uninitialized.
     * </p>
     *
     * @return pre-initialized Jersey client component.
     */
    T preInitialize();

    /**
     * Get a live view of an internal client configuration state of this initializable instance.
     *
     * @return configuration live view of the internal configuration state.
     */
    ClientConfig getConfiguration();
}
