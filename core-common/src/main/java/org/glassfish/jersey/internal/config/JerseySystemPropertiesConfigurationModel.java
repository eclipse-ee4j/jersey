/*
 * Copyright (c) 2019, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.config;

import java.util.Arrays;
import java.util.List;

class JerseySystemPropertiesConfigurationModel extends SystemPropertiesConfigurationModel {

    static final List<String> PROPERTY_CLASSES = Arrays.asList(
            "org.glassfish.jersey.CommonProperties",
            "org.glassfish.jersey.ExternalProperties",
            "org.glassfish.jersey.server.ServerProperties",
            "org.glassfish.jersey.client.ClientProperties",
            "org.glassfish.jersey.servlet.ServletProperties",
            "org.glassfish.jersey.message.MessageProperties",
            "org.glassfish.jersey.apache.connector.ApacheClientProperties",
            "org.glassfish.jersey.apache5.connector.Apache5ClientProperties",
            "org.glassfish.jersey.helidon.connector.HelidonClientProperties",
            "org.glassfish.jersey.jdk.connector.JdkConnectorProperties",
            "org.glassfish.jersey.jetty.connector.JettyClientProperties",
            "org.glassfish.jersey.jnh.connector.JavaNetHttpClientProperties",
            "org.glassfish.jersey.netty.connector.NettyClientProperties",
            "org.glassfish.jersey.media.multipart.MultiPartProperties",
            "org.glassfish.jersey.server.oauth1.OAuth1ServerProperties");

    JerseySystemPropertiesConfigurationModel() {
        super(PROPERTY_CLASSES);
    }
}