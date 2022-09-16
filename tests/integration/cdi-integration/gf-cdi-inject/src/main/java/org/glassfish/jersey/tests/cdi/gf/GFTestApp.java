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

package org.glassfish.jersey.tests.cdi.gf;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/test")
public class GFTestApp extends ResourceConfig {
    public static final String RELOADER = "RELOADER";
    private Reloader reloader = new Reloader();

    public GFTestApp() {
        super(GFTestResource.class);
        register(reloader);

        property(CommonProperties.PROVIDER_DEFAULT_DISABLE, "ALL");
        property(RELOADER, reloader);
    }

    static class Reloader implements ContainerLifecycleListener {
        Container container;

        @Override
        public void onStartup(Container container) {
            this.container = container;
        }

        @Override
        public void onReload(Container container) {

        }

        @Override
        public void onShutdown(Container container) {

        }
    }
}
