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

package org.glassfish.jersey.client.oauth1;

import java.util.Arrays;

/**
 * Access Token class (credentials issued by the Service Provider for the user).
 * The class stores client secret as byte array to improve security.
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
public final class AccessToken {

    private final String token;
    private final byte[] accessTokenSecret;


    /**
     * Create a new access token.
     *
     * @param token Access token.
     * @param accessTokenSecret Access token secret.
     */
    public AccessToken(final String token, final String accessTokenSecret) {
        this.token = token;
        this.accessTokenSecret = accessTokenSecret.getBytes();
    }

    /**
     * Create a new access token with secret defined as byte array.
     *
     * @param token Access token.
     * @param accessTokenSecret Access token secret as byte array in the default encoding.
     */
    public AccessToken(final String token, final byte[] accessTokenSecret) {
        this.token = token;
        this.accessTokenSecret = accessTokenSecret;
    }

    /**
     * Get the access token.
     *
     * @return Access token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Get the access token secret.
     * @return Secret part of access token.
     */
    public String getAccessTokenSecret() {
        return new String(accessTokenSecret);
    }

    /**
     * Get the access token secret in byte arrays (in default encoding).
     * @return Byte array with access token secret.
     */
    public byte[] getAccessTokenSecretAsByteArray() {
        return accessTokenSecret;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AccessToken that = (AccessToken) o;

        if (!Arrays.equals(accessTokenSecret, that.accessTokenSecret)) {
            return false;
        }
        if (!token.equals(that.token)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = token.hashCode();
        result = 31 * result + Arrays.hashCode(accessTokenSecret);
        return result;
    }
}
