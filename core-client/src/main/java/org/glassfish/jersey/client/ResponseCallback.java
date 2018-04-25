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

import org.glassfish.jersey.process.internal.RequestScope;

/**
 * Client response processing callback.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
interface ResponseCallback {

    /**
     * Called when the client invocation was successfully completed with a response.
     *
     * @param response response data.
     * @param scope request processing scope.
     */
    public void completed(ClientResponse response, RequestScope scope);

    /**
     * Called when the invocation has failed for any reason.
     *
     * @param error contains failure details.
     */
    public void failed(ProcessingException error);
}
