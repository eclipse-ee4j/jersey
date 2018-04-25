/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.selectable;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.filtering.SelectableEntityFilteringFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Entity Data Filtering application using request parameters.
 *
 * @author Andy Pemberton (pembertona at gmail.com)
 * @author Michal Gajdos
 */
@ApplicationPath("/")
public class SelectableEntityFilteringApplication extends ResourceConfig {

    public SelectableEntityFilteringApplication() {
        // Register all resources present under the package.
        packages("org.glassfish.jersey.examples.entityfiltering.selectable");

        // Register entity-filtering selectable feature.
        register(SelectableEntityFilteringFeature.class);
        property(SelectableEntityFilteringFeature.QUERY_PARAM_NAME, "select");

        // Configure MOXy Json provider. Comment this line to use Jackson. Uncomment to use MOXy.
        register(new MoxyJsonConfig().setFormattedOutput(true).resolver());

        // Configure Jackson Json provider. Comment this line to use MOXy. Uncomment to use Jackson.
        // register(JacksonFeature.class);
    }
}
