/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.oauth1.signature;

/**
 * Thrown to indicate that an OAuth exception occurred.
 *
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public class OAuth1SignatureException extends Exception {

    /**
     * Constructs an OAuth signature exception with no detail message.
     */
    public OAuth1SignatureException() {
        super();
    }

    /**
     * Constructs an OAuth signature exception with the specified detail
     * message.
     *
     * @param s the detail message.
     */
    public OAuth1SignatureException(String s) {
        super(s);
    }

   /**
     * Constructs an OAuth signature exception with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public OAuth1SignatureException(Throwable cause) {
        super(cause);
    }
}

