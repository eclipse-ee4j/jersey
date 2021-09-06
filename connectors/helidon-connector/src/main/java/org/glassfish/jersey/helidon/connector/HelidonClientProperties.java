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
package org.glassfish.jersey.helidon.connector;

import org.glassfish.jersey.internal.util.PropertiesClass;

import io.helidon.config.Config;
import io.helidon.webclient.WebClient;

/**
 * Configuration options specific to the Client API that utilizes {@link HelidonConnectorProvider}
 * @since 2.31
 */
@PropertiesClass
public final class HelidonClientProperties {

    /**
     * A Helidon {@link Config} instance that is passed to {@link WebClient.Builder#config(Config)} if available
     */
    public static final String CONFIG = io.helidon.jersey.connector.HelidonProperties.CONFIG;
}
