/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.gson;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.gson.internal.JsonGsonAutoDiscoverable;
import org.glassfish.jersey.gson.internal.JsonGsonProvider;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
/**
 * Feature used to register Gson providers.
 * <p>
 * The Feature is automatically enabled when {@link JsonGsonAutoDiscoverable} is on classpath.
 * Default GSON configuration obtained by calling {@code GsonBuilder.create()} is used.
 * <p>
 * Custom configuration, if required, can be achieved by implementing custom {@link javax.ws.rs.ext.ContextResolver} and
 * registering it as a provider into JAX-RS runtime:
 * <pre>
 * &#64;Provider
 * &#64;class GsonContextResolver implements ContextResolver&lt;Gson&gt; {
 *      &#64;Override
 *      public Gson getContext(Class<?> type) {
 *          GsonBuilder builder = new GsonBuilder();
 *          // add custom configuration
 *          return builder.create();
 *      }
 * }
 * </pre>
 *
 */
public class JsonGsonFeature implements Feature {

    private static final String JSON_FEATURE = JsonGsonFeature.class.getSimpleName();

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        final String jsonFeature = CommonProperties.getValue(
                config.getProperties(),
                config.getRuntimeType(),
                InternalProperties.JSON_FEATURE, JSON_FEATURE, String.class);

        // Other JSON providers registered.
        if (!JSON_FEATURE.equalsIgnoreCase(jsonFeature)) {
            return false;
        }

        // Disable other JSON providers.
        context.property(PropertiesHelper.getPropertyNameForRuntime(
                InternalProperties.JSON_FEATURE, config.getRuntimeType()), JSON_FEATURE);

        context.register(JsonGsonProvider.class);

        return true;
    }
}
