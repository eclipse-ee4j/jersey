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
 * Contains the secrets used to generate and/or verify signatures.
 *
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public class OAuth1Secrets {

    /** The consumer secret. */
    private volatile String consumerSecret;

    /** The request or access token secret. */
    private volatile String tokenSecret;

    /**
     * Returns the consumer secret.
     */
    public String getConsumerSecret() {
        return consumerSecret;
    }

    /*
     * Sets the consumer secret.
     */
    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    /**
     * Builder pattern method to return {@link OAuth1Secrets} after setting
     * consumer secret.
     *
     * @param consumerSecret the consumer secret.
     */
    public OAuth1Secrets consumerSecret(String consumerSecret) {
        setConsumerSecret(consumerSecret);
        return this;
    }

    /**
     * Returns request or access token.
     */
    public String getTokenSecret() {
        return tokenSecret;
    }

    /**
     * Sets request or access token.
     */
    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    /**
     * Builder pattern method to return {@link OAuth1Secrets} after setting
     * consumer secret.
     *
     * @param tokenSecret the token secret.
     */
    public OAuth1Secrets tokenSecret(String tokenSecret) {
        setTokenSecret(tokenSecret);
        return this;
    }

    @Override
    public OAuth1Secrets clone() {
        return new OAuth1Secrets().consumerSecret(this.consumerSecret).tokenSecret(this.tokenSecret);
    }
}

