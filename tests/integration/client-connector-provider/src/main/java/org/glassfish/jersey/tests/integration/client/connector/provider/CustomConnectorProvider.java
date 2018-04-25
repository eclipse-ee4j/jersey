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

package org.glassfish.jersey.tests.integration.client.connector.provider;

import java.net.HttpURLConnection;

import javax.ws.rs.client.Client;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.internal.HttpUrlConnector;
import org.glassfish.jersey.client.spi.Connector;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public final class CustomConnectorProvider extends HttpUrlConnectorProvider {

    public static volatile boolean invoked = false;

    @Override
    protected Connector createHttpUrlConnector(Client client, ConnectionFactory connectionFactory, int chunkSize,
                                               boolean fixLengthStreaming, boolean setMethodWorkaround) {

        return new HttpUrlConnector(
                client,
                connectionFactory,
                chunkSize,
                fixLengthStreaming,
                setMethodWorkaround) {

            @Override
            protected void secureConnection(JerseyClient client, HttpURLConnection uc) {
                invoked = true;
            }
        };
    }
}
