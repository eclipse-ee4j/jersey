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

package org.glassfish.jersey.server.spring.test;

import javax.ws.rs.core.Application;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.PerLookup;
import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * JAX-RS application class for configuring injectable services in HK2 registry for testing purposes.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
public class MyApplication extends Application {

    @Inject
    public MyApplication(final InjectionManager injectionManager) {
        Binder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(HK2ServiceSingleton.class).in(Singleton.class);
                bindAsContract(HK2ServiceRequestScoped.class).in(RequestScoped.class);
                bindAsContract(HK2ServicePerLookup.class).in(PerLookup.class);
            }
        };

        injectionManager.register(binder);
    }
}
