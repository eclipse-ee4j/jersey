/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.java.connector;

import org.glassfish.jersey.internal.util.PropertiesClass;

import java.net.http.HttpClient;

/**
 * Provides configuration properties for a {@link JavaConnector}.
 *
 * @author Steffen Nießing
 */
@PropertiesClass
public class JavaClientProperties {
    /**
     * Configuration of the {@link java.net.CookieHandler} that should be used by the {@link HttpClient}.
     * If this option is not set, {@link HttpClient#cookieHandler()} will return an empty {@link java.util.Optional}
     * and therefore no cookie handler will be used.
     *
     * A provided value to this option has to be of type {@link java.net.CookieHandler}.
     */
    public static final String COOKIE_HANDLER = "jersey.config.java.client.cookieHandler";

    /**
     * Configuration of SSL parameters used by the {@link HttpClient}.
     * If this option is not set, then the {@link HttpClient} will use <it>implementation specific</it> default values.
     *
     * A provided value to this option has to be of type {@link javax.net.ssl.SSLParameters}.
     */
    public static final String SSL_PARAMETERS = "jersey.config.java.client.sslParameters";

    /**
     * Prevent this class from instantiation.
     */
    private JavaClientProperties() {}
}
