/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.micrometer.server;

import io.micrometer.common.KeyValues;
import io.micrometer.common.lang.Nullable;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.monitoring.RequestEvent;

/**
 * Default implementation for {@link JerseyObservationConvention}.
 *
 * @author Marcin Grzejszczak
 * @since 2.41
 */
public class DefaultJerseyObservationConvention implements JerseyObservationConvention {

    private final String metricsName;

    public DefaultJerseyObservationConvention(String metricsName) {
        this.metricsName = metricsName;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(JerseyContext context) {
        RequestEvent event = context.getRequestEvent();
        ContainerRequest request = context.getCarrier();
        ContainerResponse response = context.getResponse();
        return KeyValues.of(JerseyKeyValues.method(request), JerseyKeyValues.uri(event),
                JerseyKeyValues.exception(event), JerseyKeyValues.status(response), JerseyKeyValues.outcome(response));
    }

    @Override
    public String getName() {
        return this.metricsName;
    }

    @Nullable
    @Override
    public String getContextualName(JerseyContext context) {
        if (context.getCarrier() == null) {
            return null;
        }
        return "HTTP " + context.getCarrier().getMethod();
    }

}
