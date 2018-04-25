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

package org.glassfish.jersey.client;

import javax.ws.rs.ProcessingException;

/**
 * Internal exception indicating that request processing has been aborted
 * in the request filter processing chain.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 *
 * @see javax.ws.rs.client.ClientRequestContext#abortWith(javax.ws.rs.core.Response)
 */
class AbortException extends ProcessingException {
    private final transient ClientResponse abortResponse;

    /**
     * Create new abort exception.
     *
     * @param abortResponse abort response.
     */
    AbortException(ClientResponse abortResponse) {
        super("Request processing has been aborted");
        this.abortResponse = abortResponse;
    }

    /**
     * Get the abort response that caused this exception.
     *
     * @return abort response.
     */
    public ClientResponse getAbortResponse() {
        return abortResponse;
    }
}
