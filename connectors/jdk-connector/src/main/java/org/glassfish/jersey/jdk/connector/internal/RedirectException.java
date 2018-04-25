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

import org.glassfish.jersey.client.ClientProperties;

/**
 * This Exception is used only if {@link ClientProperties#FOLLOW_REDIRECTS} is set to {@code true}.
 * <p/>
 * This exception is thrown when any of the Redirect HTTP response status codes (301, 302, 303, 307, 308) is received and:
 * <ul>
 * <li>
 * the chained redirection count exceeds the value of
 * {@link org.glassfish.jersey.client.JdkConnectorProvider#MAX_REDIRECTS}
 * </li>
 * <li>
 * or an infinite redirection loop is detected
 * </li>
 * <li>
 * or Location response header is missing, empty or does not contain a valid {@link java.net.URI}.
 * </li>
 * </ul>
 *
 * @author Ondrej Kosatka (ondrej.kosatka at oracle.com)
 * @see RedirectHandler
 */
public class RedirectException extends Exception {

    private static final long serialVersionUID = 4357724300486801294L;

    /**
     * Constructor.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public RedirectException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public RedirectException(String message, Throwable t) {
        super(message, t);
    }
}
