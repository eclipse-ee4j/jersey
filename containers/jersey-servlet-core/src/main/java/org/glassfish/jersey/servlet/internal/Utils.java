/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.servlet.internal;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Utility class.
 *
 * @author Michal Gajdos
 */
public final class Utils {

    /**
     * Internal {@link javax.servlet.ServletContext servlet context} attribute name under which an instance of
     * {@link org.glassfish.jersey.server.ResourceConfig resource config} can be stored. The instance is later used to initialize
     * servlet in {@link org.glassfish.jersey.servlet.WebConfig} instead of creating a new one.
     */
    private static final String RESOURCE_CONFIG = "jersey.config.servlet.internal.resourceConfig";

    /**
     * Store {@link org.glassfish.jersey.server.ResourceConfig resource config} as an attribute of given
     * {@link javax.servlet.ServletContext servlet context}. If {@code config} is {@code null} then the previously stored value
     * (if any) is removed. The {@code configName} is used as an attribute name suffix.
     *
     * @param config resource config to be stored.
     * @param context servlet context to store the config in.
     * @param configName name or id of the resource config.
     */
    public static void store(final ResourceConfig config, final ServletContext context, final String configName) {
        final String attributeName = RESOURCE_CONFIG + "_" + configName;
        context.setAttribute(attributeName, config);
    }

    /**
     * Load {@link org.glassfish.jersey.server.ResourceConfig resource config} from given
     * {@link javax.servlet.ServletContext servlet context}. If found then the resource config is also removed from servlet
     * context. The {@code configName} is used as an attribute name suffix.
     *
     * @param context servlet context to load resource config from.
     * @param configName name or id of the resource config.
     * @return previously stored resource config or {@code null} if no resource config has been stored.
     */
    public static ResourceConfig retrieve(final ServletContext context, final String configName) {
        final String attributeName = RESOURCE_CONFIG + "_" + configName;
        final ResourceConfig config = (ResourceConfig) context.getAttribute(attributeName);
        context.removeAttribute(attributeName);
        return config;
    }

    /**
     * Extract context params from {@link ServletContext}.
     *
     * @param servletContext actual servlet context.
     * @return map representing current context parameters.
     */
    public static Map<String, Object> getContextParams(final ServletContext servletContext) {
        final Map<String, Object> props = new HashMap<>();
        final Enumeration names = servletContext.getAttributeNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            props.put(name, servletContext.getAttribute(name));
        }
        return props;
    }

    /**
     * Prevents instantiation.
     */
    private Utils() {
    }
}
