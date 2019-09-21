/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jsonp;

import javax.ws.rs.Priorities;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.CommonProperties;

import org.glassfish.json.jaxrs.JsonValueBodyReader;
import org.glassfish.json.jaxrs.JsonValueBodyWriter;

/**
 * {@link Feature} used to register JSON-P providers.
 *
 * @author Michal Gajdos
 */
public class JsonProcessingFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        if (CommonProperties.getValue(context.getConfiguration().getProperties(), context.getConfiguration().getRuntimeType(),
                CommonProperties.JSON_PROCESSING_FEATURE_DISABLE, Boolean.FALSE, Boolean.class)) {
            return false;
        }

        // Make sure JSON-P workers have higher priority than other Json providers (in case there is a need to use JSON-P and some
        // other provider in an application).
        context.register(JsonValueBodyReader.class, Priorities.USER + 1000);
        context.register(JsonValueBodyWriter.class, Priorities.USER + 1000);

        return true;
    }
}
