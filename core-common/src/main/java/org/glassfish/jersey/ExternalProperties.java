/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey;

import org.glassfish.jersey.internal.util.PropertiesClass;

@PropertiesClass
public final class ExternalProperties {

    /**
     * Property used to specify the hostname, or address, of the proxy server.
     */
    public static final String HTTP_PROXY_HOST = "http.proxyHost";

    /**
     * Property used to specify the port number of the proxy server.
     */
    public static final String HTTP_PROXY_PORT = "http.proxyPort";

    /**
     * Property used to indicates the hosts that should be accessed
     * without going through the proxy.
     */
    public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    /**
     * Prevent instantiation.
     */
    private ExternalProperties() {
    }

}
