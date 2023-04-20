/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector;

import org.glassfish.jersey.internal.util.PropertiesClass;

import java.net.http.HttpClient;

/**
 * Provides configuration properties for a {@link JavaNetHttpConnector}.
 *
 * @author Steffen Nießing
 */
@PropertiesClass
public class JavaNetHttpClientProperties {
    /**
     * <p>
     *     Configuration of the {@link java.net.CookieHandler} that should be used by the {@link HttpClient}.
     *     If this option is not set, {@link HttpClient#cookieHandler()} will return an empty {@link java.util.Optional}
     *     and therefore no cookie handler will be used.
     * </p>
     * <p>
     *     A provided value to this option has to be of type {@link java.net.CookieHandler}.
     * </p>
     * <p>
     *     The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String COOKIE_HANDLER = "jersey.config.jnh.client.cookieHandler";

    /**
     * <p>
     *     Configuration of SSL parameters used by the {@link HttpClient}.
     *     If this option is not set, then the {@link HttpClient} will use <it>implementation specific</it> default values.
     * </p>
     * <p>
     *     A provided value to this option has to be of type {@link javax.net.ssl.SSLParameters}.
     * </p>
     * <p>
     *     The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String SSL_PARAMETERS = "jersey.config.jnh.client.sslParameters";

    /**
     * <p>
     *     An instance of the {@link java.net.Authenticator} class that should be used to retrieve
     *     credentials from a user.
     * </p>
     * <p>
     *     The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String PREEMPTIVE_BASIC_AUTHENTICATION =
            "jersey.config.jnh.client.preemptiveBasicAuthentication";

    /**
     * <p>
     *     A value of {@code false} indicates the client should handle cookies
     *     automatically using HttpClient's default cookie policy. A value
     *     of {@code false} will cause the client to ignore all cookies.
     * </p>
     * <p>
     *     The value MUST be an instance of {@link java.lang.Boolean}.
     *     If the property is absent the default value is {@code false}
     * </p>
     * <p>
     *     The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     */
    public static final String DISABLE_COOKIES =
            "jersey.config.jnh.client.disableCookies";

    /**
     * Prevent this class from instantiation.
     */
    private JavaNetHttpClientProperties() {}
}
