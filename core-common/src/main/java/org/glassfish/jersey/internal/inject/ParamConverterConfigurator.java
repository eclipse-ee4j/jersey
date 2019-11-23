/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 Payara Foundation and/or its affiliates.
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

package org.glassfish.jersey.internal.inject;

import javax.ws.rs.ext.ParamConverterProvider;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;

import static org.glassfish.jersey.internal.inject.Bindings.service;

/**
 * Configurator which initializes and register {@link ParamConverters.AggregatedProvider} instances into {@link InjectionManager}.
 *
 * @author Petr Bouda
 */
public class ParamConverterConfigurator implements BootstrapConfigurator {

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        register(injectionManager, new ParamConverters.AggregatedProvider());
        register(injectionManager, new ParamConverters.CollectionParamProvider(injectionManager));
    }

    private static <P extends ParamConverterProvider> void register(InjectionManager injectionManager, P paramConverterProvider) {
        InstanceBinding<P> binding = service(paramConverterProvider).to(ParamConverterProvider.class);
        injectionManager.register(binding);
    }
}
