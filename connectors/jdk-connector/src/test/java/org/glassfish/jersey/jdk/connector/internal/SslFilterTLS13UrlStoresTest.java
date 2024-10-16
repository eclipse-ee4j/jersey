/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import org.glassfish.jersey.SslConfigurator;

public class SslFilterTLS13UrlStoresTest extends SslFilterTest {

    public SslFilterTLS13UrlStoresTest() {
        System.setProperty("jdk.tls.server.protocols", "TLSv1.3");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.3");
    }

    @Override
    protected SslConfigurator getSslConfigurator() {
        return SslConfigurator.newInstance()
                .trustStoreUrl(this.getClass().getResource("/truststore_client"))
                .trustStorePassword("asdfgh")
                .keyStoreUrl(this.getClass().getResource("/keystore_client"))
                .keyStorePassword("asdfgh");
    }
}
