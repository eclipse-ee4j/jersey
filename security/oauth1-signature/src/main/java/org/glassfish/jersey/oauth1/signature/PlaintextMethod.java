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
 * An OAuth signature method that implements Plaintext.
 *
 * @author Paul C. Bryan
 */
public final class PlaintextMethod implements OAuth1SignatureMethod {

    /**
     * Method name.
     */
    public static final String NAME = "PLAINTEXT";

    @Override
    public String name() {
        return NAME;
    }

    /**
     * Generates the PLAINTEXT signature.
     *
     * @param baseString the OAuth elements to sign (ignored).
     * @param secrets the shared secrets used to sign the request.
     * @return the plaintext OAuth signature.
     */
    public String sign(String baseString, OAuth1Secrets secrets) {
        StringBuffer buf = new StringBuffer();
        String secret = secrets.getConsumerSecret();
        if (secret != null) {
            buf.append(secret);
        }
        buf.append('&');
        secret = secrets.getTokenSecret();
        if (secret != null) {
            buf.append(secret);
        }
        return buf.toString();
    }

    /**
     * Verifies the Plaintext signature.
     *
     * @param elements OAuth elements (ignored).
     * @param secrets the shared secrets for verifying the signature.
     * @param signature plaintext OAuth signature to be verified.
     */
    public boolean verify(String elements, OAuth1Secrets secrets, String signature) {
        return sign(elements, secrets).equals(signature);
    }
}

