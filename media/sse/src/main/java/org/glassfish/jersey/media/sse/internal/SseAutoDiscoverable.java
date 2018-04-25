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

package org.glassfish.jersey.media.sse.internal;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;

import javax.annotation.Priority;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.media.sse.SseFeature;

/**
 * Jersey {@link org.glassfish.jersey.internal.spi.AutoDiscoverable} responsible for registering {@link SseFeature}.
 *
 * If this feature is not already registered and the property
 * {@link org.glassfish.jersey.media.sse.SseFeature#DISABLE_SSE} is not set to {@code true}, the {@code SseFeature}
 * will be automatically registered.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public final class SseAutoDiscoverable implements ForcedAutoDiscoverable {
    @Override
    public void configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();
        if (context.getConfiguration().isRegistered(SseFeature.class)) {
            return;
        }

        if (!PropertiesHelper.getValue(
                config.getProperties(), config.getRuntimeType(), SseFeature.DISABLE_SSE, Boolean.FALSE, Boolean.class, null)) {
            context.register(SseFeature.class);
        }
    }
}
