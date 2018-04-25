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

package org.glassfish.jersey.server.oauth1;

import java.security.Principal;

/**
 * Class representing a registered consumer.
 *
 * @author Martin Matula
 */
public interface OAuth1Consumer {
    /** Returns consumer key.
     *
     * @return consumer key
     */
    String getKey();

    /** Returns consumer secret.
     *
     * @return consumer secret
     */
    String getSecret();

    /** Returns a {@link java.security.Principal} object representing this consumer.
     * When the oauth filter verifies the request
     * and no access token is provided, this is the principal that will get set to the security context.
     * This can be used for 2-legged oauth. If the server does not allow consumers acting
     * on their own (with no access token), this method should return null.
     *
     * @return Principal corresponding to this consumer, or null if 2-legged oauth not supported (i.e. consumers can't act on their own)
     */
    Principal getPrincipal();

    /** Returns a boolean indicating whether this consumer is authorized for the
     * specified logical "role". When the oauth filter verifies the request
     * and no access token is provided (2-legged oauth), it sets the consumer object to the security context
     * which then delegates {@link javax.ws.rs.core.SecurityContext#isUserInRole(String)} to this
     * method.
     *
     * @param role a {@code String} specifying the name of the role
     *
     * @return a {@code boolean} indicating whether this token is authorized for
     * a given role
     */
    boolean isInRole(String role);
}
