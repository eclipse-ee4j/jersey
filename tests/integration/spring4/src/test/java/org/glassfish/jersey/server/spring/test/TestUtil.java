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

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.PerLookup;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

class TestUtil {

    public static ResourceConfig registerHK2Services(final ResourceConfig rc) {
        rc
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(BuilderHelper.link(HK2ServiceSingleton.class).in(Singleton.class).build());
                    }
                })
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(BuilderHelper.link(HK2ServiceRequestScoped.class).in(RequestScoped.class).build());
                    }
                })
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(BuilderHelper.link(HK2ServicePerLookup.class).in(PerLookup.class).build());
                    }
                });
        return rc;
    }
}
