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

package org.glassfish.jersey.client.oauth2;

/**
 * Client Identifier that contains information about client id and client secret issues by a
 * Service Provider for application. The class stores client secret as byte array to improve security.
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
public class ClientIdentifier {
    private final String clientId;
    private final byte[] clientSecret;


    /**
     * Create a new instance initialized with client id and client secret in form of String value.
     *
     * @param clientId Client id.
     * @param clientSecret Client secret id.
     */
    public ClientIdentifier(final String clientId, final String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret.getBytes();
    }

    /**
     * Create a new instance initialized with client id and client secret in form of byte array.
     *
     * @param clientId Client id.
     * @param clientSecret Client secret id as a byte array value in the default encoding.
     */
    public ClientIdentifier(final String clientId, final byte[] clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Get the client id.
     *
     * @return Client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get client secret.
     *
     * @return Client secret as a String.
     */
    public String getClientSecret() {
        return new String(clientSecret);
    }

    /**
     * Get client secret as byte array.
     *
     * @return Client secret as a byte array.
     */
    public byte[] getClientSecretAsByteArray() {
        return clientSecret;
    }
}


