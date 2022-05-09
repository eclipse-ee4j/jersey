/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache5.connector;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.glassfish.jersey.spi.Contract;

/**
 * A callback interface used to configure {@link org.apache.hc.client5.http.impl.classic.HttpClientBuilder}. It is called immediately before
 * the {@link Apache5ConnectorProvider} creates {@link org.apache.hc.client5.http.classic.HttpClient}, after the
 * {@link org.apache.hc.client5.http.impl.classic.HttpClientBuilder} is configured using the properties.
 */
@Contract
public interface Apache5HttpClientBuilderConfigurator {
    /**
     * A callback method to configure the {@link org.apache.hc.client5.http.impl.classic.HttpClientBuilder}
     * @param httpClientBuilder {@link org.apache.hc.client5.http.impl.classic.HttpClientBuilder} object to be further configured
     * @return the configured {@link org.apache.hc.client5.http.impl.classic.HttpClientBuilder}. If {@code null} is returned the
     * {@code httpClientBuilder} is used by {@link Apache5ConnectorProvider} instead.
     */
    HttpClientBuilder configure(HttpClientBuilder httpClientBuilder);
}
