/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

/**
 * @author Michal Gajdos
 */
public class Application extends ResourceConfig {

    public Application() {
        register(HelloWorldResource.class);

        // Turn off Monitoring to not affect benchmarks.
        property(ServerProperties.MONITORING_ENABLED, false);
        property(ServerProperties.MONITORING_STATISTICS_ENABLED, false);
        property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, false);
    }
}
