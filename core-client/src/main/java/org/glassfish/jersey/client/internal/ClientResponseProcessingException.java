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

package org.glassfish.jersey.client.internal;

import org.glassfish.jersey.client.ClientResponse;

import javax.ws.rs.ProcessingException;

/**
 * This is a representation of a @{link ProcessingException} containing a @{link ClientResponse} instance.
 * This exception is meant to be converted to a {@code ResponseProcessingException} at a point where
 * {@link ClientResponse} is converted to a {@code Response} before it is delivered to a user.
 * @since 2.31
 */
public class ClientResponseProcessingException extends ProcessingException {
    private static final long serialVersionUID = 3389677946623416847L;
    private final ClientResponse clientResponse;

    /**
     * An instance of {@code ClientResponseProcessingException} containing {@link ClientResponse} and cause {@link Throwable}.
     * @param clientResponse a {@link ClientResponse} to be converted to {@code Response}.
     * @param cause a cause of the exception.
     */
    public ClientResponseProcessingException(ClientResponse clientResponse, Throwable cause) {
        super(cause);
        this.clientResponse = clientResponse;
    }

    /**
     * Return a {@link ClientResponse} to be converted to {@code Response} to be put to a {@code ResponseProcessingException}.
     * @return a {@link ClientResponse} to be converted to {@code Response}.
     */
    public ClientResponse getClientResponse() {
        return clientResponse;
    }
}
