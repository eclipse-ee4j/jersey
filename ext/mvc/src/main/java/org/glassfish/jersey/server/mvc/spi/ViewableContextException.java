/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.mvc.spi;

import org.glassfish.jersey.server.ContainerException;

/**
 * A runtime exception associated with errors when resolving a {@link org.glassfish.jersey.server.mvc.Viewable} to a {@link
 * ResolvedViewable} by methods on {@link ViewableContext}.
 *
 * @author Paul Sandoz
 */
@SuppressWarnings("UnusedDeclaration")
public class ViewableContextException extends ContainerException {

    /**
     * Construct a new instance with the supplied message.
     *
     * @param message the message.
     */
    public ViewableContextException(String message) {
        super(message);
    }

    /**
     * Construct a new instance with the supplied message and cause.
     *
     * @param message the message.
     * @param cause the Throwable that caused the exception to be thrown.
     */
    public ViewableContextException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a new instance with the supplied cause.
     *
     * @param cause the Throwable that caused the exception to be thrown.
     */
    public ViewableContextException(Throwable cause) {
        super(cause);
    }
}
