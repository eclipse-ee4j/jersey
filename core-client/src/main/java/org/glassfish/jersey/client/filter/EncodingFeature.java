/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.filter;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.spi.ContentEncoder;

/**
 * Feature that configures support for content encodings on the client side.
 * This feature registers {@link EncodingFilter} and the specified set of
 * {@link org.glassfish.jersey.spi.ContentEncoder encoding providers} to the
 * {@link javax.ws.rs.core.Configurable client configuration}. It also allows
 * setting the value of {@link ClientProperties#USE_ENCODING} property.
 *
 * @author Martin Matula
 */
public class EncodingFeature implements Feature {
    private final String useEncoding;
    private final Class<?>[] encodingProviders;

    /**
     * Create a new instance of the feature.
     *
     * @param encodingProviders Encoding providers to be registered in the client configuration.
     */
    public EncodingFeature(Class<?>... encodingProviders) {
        this(null, encodingProviders);
    }

    /**
     * Create a new instance of the feature specifying the default value for the
     * {@link ClientProperties#USE_ENCODING} property. Unless the value is set in the client configuration
     * properties at the time when this feature gets enabled, the provided value will be used.
     *
     * @param useEncoding Default value of {@link ClientProperties#USE_ENCODING} property.
     * @param encoders    Encoders to be registered in the client configuration.
     */
    public EncodingFeature(String useEncoding, Class<?>... encoders) {
        this.useEncoding = useEncoding;

        Providers.ensureContract(ContentEncoder.class, encoders);
        this.encodingProviders = encoders;
    }


    @Override
    public boolean configure(FeatureContext context) {
        if (useEncoding != null) {
            // properties take precedence over the constructor value
            if (!context.getConfiguration().getProperties().containsKey(ClientProperties.USE_ENCODING)) {
                context.property(ClientProperties.USE_ENCODING, useEncoding);
            }
        }
        for (Class<?> provider : encodingProviders) {
            context.register(provider);
        }
        boolean enable = useEncoding != null || encodingProviders.length > 0;
        if (enable) {
            context.register(EncodingFilter.class);
        }
        return enable;
    }
}
