/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon.connector;

import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.internal.util.JdkVersion;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;
import java.io.OutputStream;

/**
 * Helidon Connector stub which only throws exception when running on JDK prior to 21.
 * New Helidon 3 does not support JDKs prior to 21.
 *
 * @since 3.0.5
 */
public class HelidonConnectorProvider implements ConnectorProvider {
    @Override
    public Connector getConnector(Client client, Configuration runtimeConfig) {
        if (JdkVersion.getJdkVersion().getMajor() < 21) {
            throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
        }
        return null;
    }
}
