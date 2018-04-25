/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.freemarker;

import org.glassfish.jersey.examples.freemarker.resources.FreemarkerResource;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;

/**
 * Freemarker application configuration.
 *
 * @author Michal Gajdos
 */
public class MyApplication extends ResourceConfig {

    public MyApplication() {
        super(FreemarkerResource.class);

        register(LoggingFeature.class);
        register(FreemarkerMvcFeature.class);
    }
}
