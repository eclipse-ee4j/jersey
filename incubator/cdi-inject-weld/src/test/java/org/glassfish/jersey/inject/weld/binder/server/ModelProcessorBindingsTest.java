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

package org.glassfish.jersey.inject.weld.binder.server;

import org.glassfish.jersey.inject.weld.TestParent;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerConfig;
import org.glassfish.jersey.server.internal.JerseyResourceContext;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.wadl.WadlApplicationContext;
import org.glassfish.jersey.server.wadl.internal.WadlApplicationContextImpl;
import org.glassfish.jersey.server.wadl.processor.OptionsMethodProcessor;
import org.glassfish.jersey.server.wadl.processor.WadlModelProcessor;
import org.junit.jupiter.api.Test;

import jakarta.inject.Singleton;
import javax.security.auth.login.Configuration;
import jakarta.ws.rs.container.ResourceContext;

public class ModelProcessorBindingsTest extends TestParent {

    @Test
    public void testWadlProcessor() {
        injectionManager.completeRegistration();
        injectionManager.register(Bindings.service(new OptionsMethodProcessor()).to(ModelProcessor.class));
//        injectionManager.register(new AbstractBinder() {
//            @Override
//            protected void configure() {
//                bind(new WadlModelProcessor()).to(ModelProcessor.class);
//            }
//        });
        assertMultiple(ModelProcessor.class, 2, WadlModelProcessor.class.getSimpleName(),
                OptionsMethodProcessor.class.getSimpleName());
    }

    @Test
    public void testWadlContext() {
        injectionManager.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(new ResourceConfig()).to(ServerConfig.class).to(Configuration.class).id(1000);
                bind(new JerseyResourceContext(null, null, null)).to(ResourceContext.class).id(3112);
                bind(WadlApplicationContextImpl.class).to(WadlApplicationContext.class).in(Singleton.class);
            }
        });
        injectionManager.completeRegistration();
        assertOneInstance(WadlApplicationContext.class, WadlApplicationContextImpl.class.getSimpleName());
    }
}
