/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.mvc.thymeleaf;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import org.glassfish.jersey.server.mvc.MvcFeature;

@ConstrainedTo(RuntimeType.SERVER)
public final class ThymeleafMvcFeature implements Feature {
    public static final String SUFFIX = ".thymeleaf";
    public static final String TEMPLATE_BASE_PATH = MvcFeature.TEMPLATE_BASE_PATH + SUFFIX;
    public static final String CACHE_TEMPLATES = MvcFeature.CACHE_TEMPLATES + SUFFIX;
    public static final String TEMPLATE_OBJECT_FACTORY = MvcFeature.TEMPLATE_OBJECT_FACTORY + SUFFIX;
    public static final String ENCODING = MvcFeature.ENCODING + SUFFIX;

    public static final String TEMPLATE_FILE_SUFFIX = "jersey.config.server.mvc.templateFileSuffix" + SUFFIX;
    public static final String TEMPLATE_MODE = "jersey.config.server.mvc.templateMode" + SUFFIX;
    public static final String CACHE_TTLMS = "jersey.config.server.mvc.cacheTTLMs" + SUFFIX;

    @Override
    public boolean configure(FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (!config.isRegistered(ThymeleafViewProcessor.class)) {
            context.register(ThymeleafViewProcessor.class);

            // MvcFeature.
            if (!config.isRegistered(MvcFeature.class)) {
                context.register(MvcFeature.class);
            }

            return true;
        }
        return false;
    }
}
