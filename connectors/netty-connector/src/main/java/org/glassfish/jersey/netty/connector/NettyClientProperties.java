/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector;

import org.glassfish.jersey.internal.util.PropertiesClass;

/**
 * Configuration options specific to the Client API that utilizes {@link NettyConnectorProvider}.
 *
 * @since 2.32
 */
@PropertiesClass
public class NettyClientProperties {

    /**
     * <p>
     *    This property determines the maximum number of idle connections that will be simultaneously kept alive
     *    in total, rather than per destination. The default is 60. Specify 0 to disable.
     * </p>
     */
    public static final String MAX_CONNECTIONS_TOTAL = "jersey.config.client.maxTotalConnections";

    /**
     * <p>
     *    This property determines the number of seconds the idle connections are kept in the pool before pruned.
     *    The default is 60. Specify 0 to disable.
     *  </p>
     */
    public static final String IDLE_CONNECTION_PRUNE_TIMEOUT = "jersey.config.client.idleConnectionPruneTimeout";

    /**
     *  <p>
     *    This property determines the maximum number of idle connections that will be simultaneously kept alive, per destination.
     *    The default is 5.
     *  </p>
     *  <p>
     *    This property is a Jersey alternative to System property {@code}http.maxConnections{@code}. The Jersey property takes
     *    precedence over the system property.
     *  </p>
     */
    public static final String MAX_CONNECTIONS = "jersey.config.client.maxConnections";
}
