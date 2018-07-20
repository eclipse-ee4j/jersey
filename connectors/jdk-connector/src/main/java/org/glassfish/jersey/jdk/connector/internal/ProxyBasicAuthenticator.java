/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import java.nio.charset.Charset;
import java.util.Base64;


/**
 * @author Ondrej Kosatka (ondrej.kosatka at oracle.com)
 */
class ProxyBasicAuthenticator {

    /**
     * Encoding used for authentication calculations.
     */
    private static final Charset CHARACTER_SET = Charset.forName("iso-8859-1");

    static String generateAuthorizationHeader(String userName, String password) throws ProxyAuthenticationException {
        if (userName == null) {
            throw new ProxyAuthenticationException(LocalizationMessages.PROXY_USER_NAME_MISSING());
        }

        if (password == null) {
            throw new ProxyAuthenticationException(LocalizationMessages.PROXY_PASSWORD_MISSING());
        }

        byte[] prefix = (userName + ":").getBytes(CHARACTER_SET);
        byte[] passwordBytes = password.getBytes(CHARACTER_SET);
        byte[] usernamePassword = new byte[prefix.length + passwordBytes.length];

        System.arraycopy(prefix, 0, usernamePassword, 0, prefix.length);
        System.arraycopy(passwordBytes, 0, usernamePassword, prefix.length, passwordBytes.length);

        return "Basic " + Base64.getEncoder().encodeToString(usernamePassword);
    }
}
