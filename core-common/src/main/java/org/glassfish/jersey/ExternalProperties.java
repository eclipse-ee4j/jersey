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
     * If IPv6 is available on the operating system the underlying native socket will be,
     * by default, an IPv6 socket which lets applications connect to, and accept connections from,
     * both IPv4 and IPv6 hosts.
     * However, in the case an application would rather use IPv4 only sockets, then this property can be set to true.
     * The implication is that it will not be possible for the application to communicate with IPv6 only hosts.
     */
    public static final String JAVA_NET_PREFERIPV4STACK = "java.net.preferIPv4Stack";

    /**
     * When dealing with a host which has both IPv4 and IPv6 addresses, and if IPv6 is available on the operating system,
     * the default behavior is to prefer using IPv4 addresses over IPv6 ones. This is to ensure backward compatibility,
     * for example applications that depend on the representation of an IPv4 address (e.g. 192.168.1.1).
     * This property can be set to true to change that preference and use IPv6 addresses over IPv4 ones where possible.
     */
    public static final String JAVA_NET_PREFERIPV6ADDRESSES = "java.net.preferIPv6Addresses";

    /**
     * Prevent instantiation.
     */
    private ExternalProperties() {
    }

}
