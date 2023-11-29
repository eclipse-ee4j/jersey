/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty.http2.connector;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.internal.util.JdkVersion;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.jetty.connector.LocalizationMessages;

/**
 * Provides HTTP2 enabled version of the {@link JettyConnectorProvider} for a client
 *
 * @since 2.41
 */
public class JettyHttp2ConnectorProvider extends JettyConnectorProvider {
    @Override
    public Connector getConnector(Client client, Configuration runtimeConfig) {
        if (JdkVersion.getJdkVersion().getMajor() < 17) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        return null; // does not work at JDK lower than 17
    }
}