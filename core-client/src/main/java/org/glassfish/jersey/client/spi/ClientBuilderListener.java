/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.spi;

import org.glassfish.jersey.Beta;

import javax.ws.rs.client.ClientBuilder;

/**
 * <p>
 * Implementations of this interface will be notified when new ClientBuilder
 * instances are being constructed.  This will allow implementations to register
 * providers on the ClientBuilder, and is intended for global providers.
 * </p>
 * <p>
 * In order for the ClientBuilder to call implementations of this interface,
 * the implementation must be specified such that a ServiceLoader can find it -
 * i.e. it must be specified in the <code>
 * META-INF/services/org.glassfish.jersey.client.spi.ClientBuilderListener
 * </code> file in an archive on the current thread's context classloader's
 * class path.
 * </p>
 * <p>
 * Note that the <code>onNewBuilder</code> method will be called when the
 * ClientBuilder is constructed, not when it's <code>build</code> method is
 * invoked.  This allows the caller to override global providers if they desire.
 * </p>
 * <p>
 * The ClientBuilderListener are invoked in an order given by it's {@code @Priority}.
 * The default is {@code Priorities.USER}.
 * </p>
 * @since 2.32
 */
// Must not be annotated with @Contract
@Beta
public interface ClientBuilderListener {
    void onNewBuilder(ClientBuilder builder);
}
