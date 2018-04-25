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

package org.glassfish.jersey.tests.cdi.resources;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.monitoring.MonitoringFeature;

/**
 * JAX-RS application to configure resources.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@ApplicationPath("/*")
public class MyApplication extends ResourceConfig {

    public static class MyInjection {

        private final String name;

        public MyInjection(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public MyApplication() {
        // JAX-RS resource classes
        register(AppScopedFieldInjectedResource.class);
        register(AppScopedCtorInjectedResource.class);
        register(RequestScopedFieldInjectedResource.class);
        register(RequestScopedCtorInjectedResource.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(new MyInjection("no way CDI would chime in")).to(MyInjection.class);
            }
        });

        // Jersey monitoring
        register(MonitoringFeature.class);
    }
}
