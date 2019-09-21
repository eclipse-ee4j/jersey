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

/**
 * Consumer credentials class (credentials issued by the Service Provider for the application).
 * The class stores client secret as byte array to improve security.
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
public final class ConsumerCredentials {

    private final String consumerKey;
    private final byte[] consumerSecret;


    /**
     * Create new consumer credentials.
     *
     * @param consumerKey Consumer key.
     * @param consumerSecret Consumer secret.
     */
    public ConsumerCredentials(final String consumerKey, final String consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret.getBytes();
    }

    /**
     * Create a new consumer credentials with secret defined as byte array.
     *
     * @param consumerKey Consumer key.
     * @param consumerSecret Consumer secret as byte array in the default encoding.
     */
    public ConsumerCredentials(final String consumerKey, final byte[] consumerSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    /**
     * Get consumer key.
     *
     * @return Consumer key.
     */
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * Get consumer secret.
     *
     * @return Consumer secret.
     */
    public String getConsumerSecret() {
        return new String(consumerSecret);
    }

    /**
     * Get consumer secret as a byte array (in default encoding).
     *
     * @return Consumer secret.
     */
    public byte[] getConsumerSecretAsByteArray() {
        return consumerSecret;
    }

}
