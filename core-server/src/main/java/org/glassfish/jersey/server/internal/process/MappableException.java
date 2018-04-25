/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.process;

import javax.ws.rs.ProcessingException;

/**
 * A runtime exception that contains a cause, a checked or runtime exception,
 * that may be mapped to a {@link javax.ws.rs.core.Response} instance.
 * <p>
 * The runtime will catch such exceptions and attempt to map the cause
 * exception to a registered {@link javax.ws.rs.ext.ExceptionMapper} that
 * provides an appropriate {@link javax.ws.rs.core.Response} instance.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class MappableException extends ProcessingException {

    private static final long serialVersionUID = -7326005523956892754L;

    /**
     * Construct a mappable container exception.
     *
     * @param cause the cause. If the cause is an instance of
     *     {@link MappableException} then the cause of this exception
     *     will be obtained by recursively searching though the exception
     *     causes until a cause is obtained that is not an instance of
     *     {@code MappableException}.
     */
    public MappableException(Throwable cause) {
        super(unwrap(cause));
    }

    /**
     * Construct a new mappable exception with the supplied message and cause.
     *
     * @param message the exception message.
     * @param cause the exception cause.
     */
    public MappableException(String message, Throwable cause) {
        super(message, unwrap(cause));
    }

    private static Throwable unwrap(Throwable cause) {
        if (cause instanceof MappableException) {
            do {
                MappableException mce = (MappableException) cause;
                cause = mce.getCause();
            } while (cause instanceof MappableException);
        }

        return cause;
    }
}
