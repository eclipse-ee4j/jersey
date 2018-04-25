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

package org.glassfish.jersey.jetty.connector;

import java.util.Map;

import org.glassfish.jersey.internal.util.PropertiesClass;
import org.glassfish.jersey.internal.util.PropertiesHelper;

/**
 * Configuration options specific to the Client API that utilizes {@link JettyConnectorProvider}.
 *
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
@PropertiesClass
public final class JettyClientProperties {

    /**
     * Prevents instantiation.
     */
    private JettyClientProperties() {
        throw new AssertionError("No instances allowed.");
    }

    /**
     * A value of {@code false} indicates the client should handle cookies
     * automatically using HttpClient's default cookie policy. A value
     * of {@code false} will cause the client to ignore all cookies.
     * <p/>
     * The value MUST be an instance of {@link java.lang.Boolean}.
     * If the property is absent the default value is {@code false}
     */
    public static final String DISABLE_COOKIES =
            "jersey.config.jetty.client.disableCookies";

    /**
     * The credential provider that should be used to retrieve
     * credentials from a user.
     *
     * If an {@link org.eclipse.jetty.client.api.Authentication} mechanism is found,
     * it is then used for the given request, returning an {@link org.eclipse.jetty.client.api.Authentication.Result},
     * which is then stored in the {@link org.eclipse.jetty.client.api.AuthenticationStore}
     * so that subsequent requests can be preemptively authenticated.

     * <p/>
     * The value MUST be an instance of {@link
     * org.eclipse.jetty.client.util.BasicAuthentication}.  If
     * the property is absent a default provider will be used.
     */
    public static final String PREEMPTIVE_BASIC_AUTHENTICATION =
            "jersey.config.jetty.client.preemptiveBasicAuthentication";

    /**
     * A value of {@code false} indicates the client disable a hostname verification
     * during SSL Handshake. A client will ignore CN value defined in a certificate
     * that is stored in a truststore.
     * <p/>
     * The value MUST be an instance of {@link java.lang.Boolean}.
     * If the property is absent the default value is {@code true}
     */
    public static final String ENABLE_SSL_HOSTNAME_VERIFICATION =
            "jersey.config.jetty.client.enableSslHostnameVerification";

    /**
     * Get the value of the specified property.
     *
     * If the property is not set or the real value type is not compatible with the specified value type, returns {@code null}.
     *
     * @param properties  Map of properties to get the property value from.
     * @param key         Name of the property.
     * @param type        Type to retrieve the value as.
     * @param <T>         Type of the property value.
     * @return Value of the property or {@code null}.
     *
     * @since 2.8
     */
    public static <T> T getValue(final Map<String, ?> properties, final String key, final Class<T> type) {
        return PropertiesHelper.getValue(properties, key, type, null);
    }

}
