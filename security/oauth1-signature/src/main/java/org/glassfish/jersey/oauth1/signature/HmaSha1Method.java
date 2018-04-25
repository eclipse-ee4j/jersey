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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.glassfish.jersey.uri.UriComponent;


/**
 * An OAuth signature method that implements HMAC-SHA1.
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public final class HmaSha1Method implements OAuth1SignatureMethod {

    public static final String NAME = "HMAC-SHA1";

    private static final String SIGNATURE_ALGORITHM = "HmacSHA1";

    @Override
    public String name() {
        return NAME;
    }

    /**
     * Generates the HMAC-SHA1 signature of OAuth request elements.
     *
     * @param baseString the combined OAuth elements to sign.
     * @param secrets the shared secrets used to sign the request.
     * @return the OAuth signature, in base64-encoded form.
     */
    @Override
    public String sign(String baseString, OAuth1Secrets secrets) {

        Mac mac;

        try {
            mac = Mac.getInstance(SIGNATURE_ALGORITHM);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException(nsae);
        }

        StringBuilder buf = new StringBuilder();

        // null secrets are interpreted as blank per OAuth specification
        String secret = secrets.getConsumerSecret();
        if (secret != null) {
            buf.append(UriComponent.encode(secret, UriComponent.Type.UNRESERVED));
        }

        buf.append('&');

        secret = secrets.getTokenSecret();
        if (secret != null) {
            buf.append(UriComponent.encode(secret, UriComponent.Type.UNRESERVED));
        }

        byte[] key;

        try {
            key = buf.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException(uee);
        }

        SecretKeySpec spec = new SecretKeySpec(key, SIGNATURE_ALGORITHM);

        try {
            mac.init(spec);
        } catch (InvalidKeyException ike) {
            throw new IllegalStateException(ike);
        }

        return Base64.encode(mac.doFinal(baseString.getBytes()));
    }

    /**
     * Verifies the HMAC-SHA1 signature of OAuth request elements.
     *
     * @param elements OAuth elements signature is to be verified against.
     * @param secrets the shared secrets for verifying the signature.
     * @param signature base64-encoded OAuth signature to be verified.
     */
    @Override
    public boolean verify(String elements, OAuth1Secrets secrets, String signature) {
        // with symmetric cryptography, simply sign again and compare
        return sign(elements, secrets).equals(signature);
    }
}

