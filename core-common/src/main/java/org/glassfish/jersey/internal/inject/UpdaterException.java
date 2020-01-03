/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 Payara Foundation and/or its affiliates.
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

package org.glassfish.jersey.internal.inject;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

/**
 * A runtime exception that contains a cause, a checked or runtime exception,
 * that may be passed to the cause of a {@link WebApplicationException}.
 *
 * @author Paul Sandoz
 * @author Gaurav Gupta
 *
 */
public class UpdaterException extends ProcessingException {
    private static final long serialVersionUID = -4918023257104413981L;

    /**
     * Create new parameter extractor exception.
     *
     * @param message exception message.
     */
    public UpdaterException(String message) {
        super(message);
    }

    /**
     * Create new parameter extractor exception.
     *
     * @param message exception message.
     * @param cause   exception cause.
     */
    public UpdaterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create new parameter extractor exception.
     *
     * @param cause exception cause.
     */
    public UpdaterException(Throwable cause) {
        super(cause);
    }
}
