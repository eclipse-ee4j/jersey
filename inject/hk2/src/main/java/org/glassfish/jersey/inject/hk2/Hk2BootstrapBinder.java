/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.hk2;

import javax.inject.Singleton;

import org.glassfish.jersey.process.internal.RequestScope;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * {@link AbstractBinder} that registers all components needed for a proper bootstrap of Jersey based on HK2 framework.
 *
 * @author Petr Bouda
 */
public class Hk2BootstrapBinder extends AbstractBinder {

    private final ServiceLocator serviceLocator;

    /**
     * Create a bootstrap which is specific for HK2 module and automatically install {@code externalBinder}.
     *
     * @param serviceLocator HK2 service locator.
     */
    Hk2BootstrapBinder(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    protected void configure() {
        install(
                // Jersey-like class analyzer that is able to choose the right services' constructor.
                new JerseyClassAnalyzer.Binder(serviceLocator),
                // Activate possibility to start Request Scope.
                new RequestContext.Binder(),
                // Add support for Context annotation.
                new ContextInjectionResolverImpl.Binder(),
                // Improved HK2 Error reporting.
                new JerseyErrorService.Binder());

        // Register Request scope with HK2 instance.
        bind(Hk2RequestScope.class).to(RequestScope.class).in(Singleton.class);
    }
}
