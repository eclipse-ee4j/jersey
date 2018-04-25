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

package org.glassfish.jersey.client.spi;

import javax.net.ssl.SSLContext;

import org.glassfish.jersey.SslConfigurator;

/**
 * Default {@link SSLContext} provider.
 * <p>
 * Can be used to override {@link SslConfigurator#getDefaultContext()}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @since 2.21.1
 */
public interface DefaultSslContextProvider {

    /**
     * Get default {@code SSLContext}.
     * <p>
     * Returned instance is expected to be configured to container default values.
     *
     * @return default SSL context.
     * @throws IllegalStateException when there is a problem with creating or obtaining default SSL context.
     */
    SSLContext getDefaultSslContext();
}
