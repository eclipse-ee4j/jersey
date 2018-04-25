/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client.connector.ssl;

/**
 * A runtime exception representing a failure to provide correct authentication credentials.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class AuthenticationException extends RuntimeException {

    /**
     * Create new authentication exception.
     *
     * @param message error message.
     * @param realm   security realm.
     */
    public AuthenticationException(String message, String realm) {
        super(message);
        this.realm = realm;
    }

    private String realm = null;

    /**
     * Get security realm.
     *
     * @return security realm.
     */
    public String getRealm() {
        return this.realm;
    }
}
