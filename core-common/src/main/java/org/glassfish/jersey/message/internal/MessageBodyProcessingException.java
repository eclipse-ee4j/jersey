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

package org.glassfish.jersey.message.internal;

import javax.ws.rs.ProcessingException;

/**
 * Jersey exception signaling that error occurred during reading or writing message body (entity).
 *
 * @author Miroslav Fuksa
 */
public class MessageBodyProcessingException extends ProcessingException {

    private static final long serialVersionUID = 2093175681702118380L;

    /**
     * Creates new instance initialized with exception cause.
     * @param cause Exception cause.
     */
    public MessageBodyProcessingException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates new instance initialized with exception message and exception cause.
     * @param message Message.
     * @param cause Exception cause.
     */
    public MessageBodyProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates new instance initialized with exception message.
     * @param message Message.
     */
    public MessageBodyProcessingException(String message) {
        super(message);
    }
}
