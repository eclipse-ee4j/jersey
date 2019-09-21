/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.hello.spring.annotations;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

/**
 * A simple package based configuration
 *
 * @author Geoffroy Warin (http://geowarin.github.io)
 */
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(RequestContextFilter.class);
        packages("org.glassfish.jersey.examples.hello.spring.annotations");
        register(LoggingFeature.class);
    }
}
