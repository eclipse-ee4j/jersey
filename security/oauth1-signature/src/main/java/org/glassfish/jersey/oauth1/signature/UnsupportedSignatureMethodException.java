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
 * Thrown to indicate that the OAuth signature method requested is not
 * supported.
 *
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public class UnsupportedSignatureMethodException extends OAuth1SignatureException {

    /**
     * Constructs an unsupported OAuth method exception with no detail message.
     */
    public UnsupportedSignatureMethodException() {
        super();
    }

    /**
     * Constructs an unsupported OAuth method exception with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public UnsupportedSignatureMethodException(String s) {
        super(s);
    }
}

