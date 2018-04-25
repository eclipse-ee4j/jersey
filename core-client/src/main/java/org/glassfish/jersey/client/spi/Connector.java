/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.Future;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.process.Inflector;

/**
 * Client transport connector extension contract.
 * <p>
 * Note that unlike most of the other {@link org.glassfish.jersey.spi.Contract Jersey SPI extension contracts},
 * Jersey {@code Connector} is not a typical runtime extension and as such cannot be directly registered
 * using a configuration {@code register(...)} method. Jersey client runtime retrieves a {@code Connector}
 * instance upon Jersey client runtime initialization using a {@link org.glassfish.jersey.client.spi.ConnectorProvider}
 * registered in {@link org.glassfish.jersey.client.ClientConfig}.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
// Must not be annotated with @Contract
public interface Connector extends Inflector<ClientRequest, ClientResponse> {
    /**
     * Synchronously process client request into a response.
     *
     * The method is used by Jersey client runtime to synchronously send a request
     * and receive a response.
     *
     * @param request Jersey client request to be sent.
     * @return Jersey client response received for the client request.
     * @throws javax.ws.rs.ProcessingException in case of any invocation failure.
     */
    @Override
    ClientResponse apply(ClientRequest request);

    /**
     * Asynchronously process client request into a response.
     *
     * The method is used by Jersey client runtime to asynchronously send a request
     * and receive a response.
     *
     * @param request  Jersey client request to be sent.
     * @param callback Jersey asynchronous connector callback to asynchronously receive
     *                 the request processing result (either a response or a failure).
     * @return asynchronously executed task handle.
     */
    Future<?> apply(ClientRequest request, AsyncConnectorCallback callback);

    /**
     * Get name of current connector.
     *
     * Should contain identification of underlying specification and optionally version number.
     * Will be used in User-Agent header.
     *
     * @return name of current connector. Returning {@code null} or empty string means not including
     * this information in a generated <tt>{@value javax.ws.rs.core.HttpHeaders#USER_AGENT}</tt> header.
     */
    public String getName();

    /**
     * Close connector and release all it's internally associated resources.
     */
    public void close();
}
