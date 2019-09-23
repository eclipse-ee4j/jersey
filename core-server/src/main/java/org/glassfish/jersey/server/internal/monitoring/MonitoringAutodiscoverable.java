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

package org.glassfish.jersey.server.internal.monitoring;

import javax.annotation.Priority;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;
import org.glassfish.jersey.server.ServerProperties;

/**
 * Autodiscoverable feature that registers {@link MonitoringFeature}
 * based on configuration properties.
 *
 * @author Miroslav Fuksa
 */
@ConstrainedTo(RuntimeType.SERVER)
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public final class MonitoringAutodiscoverable implements ForcedAutoDiscoverable {

    @Override
    public void configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(MonitoringFeature.class)) {
            final Boolean monitoringEnabled = ServerProperties.getValue(context.getConfiguration().getProperties(),
                    ServerProperties.MONITORING_ENABLED, Boolean.FALSE);
            final Boolean statisticsEnabled = ServerProperties.getValue(context.getConfiguration().getProperties(),
                    ServerProperties.MONITORING_STATISTICS_ENABLED, Boolean.FALSE);
            final Boolean mbeansEnabled = ServerProperties.getValue(context.getConfiguration().getProperties(),
                    ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, Boolean.FALSE);

            if (monitoringEnabled || statisticsEnabled || mbeansEnabled) {
                context.register(MonitoringFeature.class);
            }
        }
    }
}
