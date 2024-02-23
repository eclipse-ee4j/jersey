/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class SslFilterTLS13Test extends SslFilterTest {

    @BeforeAll
    public static void setup() throws Exception {
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        final String[] supportedProtocols = context.getDefaultSSLParameters().getProtocols();
        if (Arrays.toString(supportedProtocols).contains("TLSv1.3")) {
           System.setProperty("jdk.tls.server.protocols", "TLSv1.3");
           System.setProperty("jdk.tls.client.protocols", "TLSv1.3");
        }
    }

    @AfterAll
    public static void tearDown() {
        System.clearProperty("jdk.tls.server.protocols");
        System.clearProperty("jdk.tls.client.protocols");
    }

}
