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
 * {@link ProcessingException Processing exception} indicating that an attempt to
 * read a value of a header failed.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class HeaderValueException extends ProcessingException {
    private static final long serialVersionUID = 981810773601231157L;
    private final Context context;

    /**
     * Context that contains header problems causing this exception (e.g. for {@link InboundMessageContext}
     * the corresponding value is {@link Context#INBOUND}).
     */
    public static enum Context {
        /**
         * Inbound context.
         */
        INBOUND,

        /**
         * Outbound context.
         */
        OUTBOUND
    }

    /**
     * Create a new header value exception from message, cause and context.
     *
     * @param message Exception message.
     * @param cause Exception cause.
     * @param context Context in which this exception was thrown.
     */
    public HeaderValueException(String message, Throwable cause, Context context) {
        super(message, cause);
        this.context = context;
    }

    /**
     * Create a new header value exception from message and context.
     *
     * @param message Exception message.
     * @param context Context in which this exception was thrown.
     */
    public HeaderValueException(String message, Context context) {
        super(message);
        this.context = context;
    }

    /**
     * Get the exception context.
     *
     * @return Context in which the exception was thrown.
     */
    public Context getContext() {
        return context;
    }
}
