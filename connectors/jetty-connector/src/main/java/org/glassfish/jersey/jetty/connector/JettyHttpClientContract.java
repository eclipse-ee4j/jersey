/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

import org.eclipse.jetty.client.HttpClient;
import org.glassfish.jersey.spi.Contract;

/**
 * A contract that allows for an optional registration of user predefined Jetty {@code HttpClient}
 * that is consequently used by {@link JettyConnector}
 */
@Contract
public interface JettyHttpClientContract {
    /**
     * Supply a user predefined HttpClient
     * @return a user predefined HttpClient
     */
    HttpClient getHttpClient();
}
