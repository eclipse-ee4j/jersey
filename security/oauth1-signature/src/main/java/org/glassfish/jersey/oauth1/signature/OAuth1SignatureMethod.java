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

import org.glassfish.jersey.spi.Contract;

/**
 * An interface representing the OAuth signature method.
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Paul C. Bryan <pbryan@sun.com>
 */
@Contract
public interface OAuth1SignatureMethod {

    /**
     * Returns the name of this signature method, as negotiated through the
     * OAuth protocol.
     *
     * @return Signature method name.
     */
     public String name();

    /**
     * Signs the data using the supplied secret(s).
     *
     * @param baseString a {@link String} that contains the request baseString to be signed.
     * @param secrets the secret(s) to use to sign the data.
     * @return a {@link String} that contains the signature.
     * @throws InvalidSecretException if a supplied secret is not valid.
     */
    public String sign(String baseString, OAuth1Secrets secrets) throws InvalidSecretException;

    /**
     * Verifies the signature for the data using the supplied secret(s).
     *
     * @param elements a {@link String} that contains the request elements to be verified.
     * @param secrets the secret(s) to use to verify the signature.
     * @param signature a {@link String} that contains the signature to be verified.
     * @return true if the signature matches the secrets and data.
     * @throws InvalidSecretException if a supplied secret is not valid.
     */
    public boolean verify(String elements, OAuth1Secrets secrets, String signature) throws InvalidSecretException;
}
