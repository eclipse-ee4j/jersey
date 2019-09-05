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

package org.glassfish.jersey.apache.connector;

import org.apache.http.impl.client.HttpClientBuilder;
import org.glassfish.jersey.spi.Contract;

/**
 * A callback interface used to configure {@link org.apache.http.impl.client.HttpClientBuilder}. It is called immediately before
 * the {@link ApacheConnectorProvider} creates {@link org.apache.http.client.HttpClient}, after the
 * {@link org.apache.http.impl.client.HttpClientBuilder} is configured using the properties.
 */
@Contract
public interface ApacheHttpClientBuilderConfigurator {
    /**
     * A callback method to configure the {@link org.apache.http.impl.client.HttpClientBuilder}
     * @param httpClientBuilder {@link org.apache.http.impl.client.HttpClientBuilder} object to be further configured
     * @return the configured {@link org.apache.http.impl.client.HttpClientBuilder}. If {@code null} is returned the
     * {@code httpClientBuilder} is used by {@link ApacheConnectorProvider} instead.
     */
    HttpClientBuilder configure(HttpClientBuilder httpClientBuilder);
}
