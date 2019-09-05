/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.internal.inject;

import javax.ws.rs.ext.ParamConverterProvider;
import org.glassfish.jersey.client.ClientBootstrapBag;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.ParamConverterFactory;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.client.inject.ParameterUpdaterProvider;

/**
 * Configurator which initializes and register {@link ParameterUpdaterProvider} instance into
 * {@link InjectionManager}.
 *
 * @author Petr Bouda
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 */
public class ParameterUpdaterConfigurator implements BootstrapConfigurator {

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        ClientBootstrapBag clientBag = (ClientBootstrapBag) bootstrapBag;

        // Param Converters must be initialized Lazy and created at the time of the call on updater
        LazyValue<ParamConverterFactory> lazyParamConverterFactory =
                Values.lazy((Value<ParamConverterFactory>) () -> new ParamConverterFactory(
                        Providers.getProviders(injectionManager, ParamConverterProvider.class),
                        Providers.getCustomProviders(injectionManager, ParamConverterProvider.class)));

        ParameterUpdaterFactory parameterUpdaterFactory = new ParameterUpdaterFactory(lazyParamConverterFactory);
        clientBag.setParameterUpdaterProvider(parameterUpdaterFactory);
        injectionManager.register(Bindings.service(parameterUpdaterFactory)
                        .to(ParameterUpdaterProvider.class));
    }
}
