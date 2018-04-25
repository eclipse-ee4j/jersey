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

package org.glassfish.jersey.client.authentication;

import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;

/**
 * Exception thrown by security response authentication.
 *
 * @author Petr Bouda
 */
public class ResponseAuthenticationException extends ResponseProcessingException {

    /**
     * Creates new instance of this exception with exception cause.
     *
     * @param response the response instance for which the processing failed.
     * @param cause Exception cause.
     */
    public ResponseAuthenticationException(Response response, Throwable cause) {
        super(response, cause);
    }

    /**
     * Creates new instance of this exception with exception message.
     *
     * @param response the response instance for which the processing failed.
     * @param message Exception message.
     */
    public ResponseAuthenticationException(Response response, String message) {
        super(response, message);
    }

    /**
     * Creates new instance of this exception with exception message and exception cause.
     *
     * @param response the response instance for which the processing failed.
     * @param message Exception message.
     * @param cause Exception cause.
     */
    public ResponseAuthenticationException(Response response, String message, Throwable cause) {
        super(response, message, cause);
    }

}
