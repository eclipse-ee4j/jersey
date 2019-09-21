/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Entity Data Filtering application.
 *
 * @author Michal Gajdos
 */
@ApplicationPath("/")
public class EntityFilteringApplication extends ResourceConfig {

    public EntityFilteringApplication() {
        // Register all resources present under the package.
        packages("org.glassfish.jersey.examples.entityfiltering");

        // Entity Data Filtering feature.
        register(EntityFilteringFeature.class);

        // Configure MOXy Json provider. Comment this line to use Jackson. Uncomment to use MOXy.
        register(new MoxyJsonConfig().setFormattedOutput(true).resolver());

        // Configure Jackson Json provider. Comment this line to use MOXy. Uncomment to use Jackson.
        // register(JacksonFeature.class);
    }
}
