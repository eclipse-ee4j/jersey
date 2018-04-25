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

package org.glassfish.jersey.test.spi;

/**
 * Thrown when a test container-specific error occurs.
 *
 * @author Paul Sandoz
 */
public class TestContainerException extends RuntimeException {
    private static final long serialVersionUID = 4116710007524221914L;

    /**
     * Construct a new instance with no message.
     */
    public TestContainerException() {
        super();
    }

    /**
     * Construct a new instance with a message.
     *
     * @param message the message
     */
    public TestContainerException(String message) {
        super(message);
    }

    /**
     * Construct a new instance with a message and a cause.
     *
     * @param message the message.
     * @param cause the Throwable that caused the exception to be thrown.
     */
    public TestContainerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a new instance with a cause.
     *
     * @param cause the Throwable that caused the exception to be thrown.
     */
    public TestContainerException(Throwable cause) {
        super(cause);
    }
}
