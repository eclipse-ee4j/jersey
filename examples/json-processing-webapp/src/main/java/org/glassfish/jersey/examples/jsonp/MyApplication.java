/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonp;

import javax.json.stream.JsonGenerator;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author Michal Gajdos
 */
public class MyApplication extends ResourceConfig {

    public MyApplication() {
        packages("org.glassfish.jersey.examples.jsonp.resource");
        register(LoggingFeature.class);
        property(JsonGenerator.PRETTY_PRINTING, true);
    }
}



