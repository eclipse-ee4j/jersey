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

package org.glassfish.jersey.internal;

import org.glassfish.jersey.internal.util.PropertiesClass;

/**
 * Internal common (server/client) Jersey configuration properties.
 *
 * @author Michal Gajdos
 */
@PropertiesClass
public class InternalProperties {

    /**
     * This property should be set by configured JSON feature to indicate that other (registered but not configured) JSON features
     * should not be configured.
     * <p/>
     * The name of the configuration property is <tt>{@value}</tt>.
     *
     * @since 2.9
     */
    public static final String JSON_FEATURE = "jersey.config.jsonFeature";

    /**
     * Client-specific version of {@link InternalProperties#JSON_FEATURE}.
     * <p/>
     * If present, it overrides the generic one for the client environment.
     *
     * @since 2.9
     */
    public static final String JSON_FEATURE_CLIENT = "jersey.config.client.jsonFeature";

    /**
     * Server-specific version of {@link InternalProperties#JSON_FEATURE}.
     * <p/>
     * If present, it overrides the generic one for the server environment.
     *
     * @since 2.9
     */
    public static final String JSON_FEATURE_SERVER = "jersey.config.server.jsonFeature";

    /**
     * Prevent instantiation.
     */
    private InternalProperties() {
    }
}
