/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.client;

import org.glassfish.jersey.client.spi.ClientComponentProvider;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.ServiceConfigurationError;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.model.internal.RankedComparator;
import org.glassfish.jersey.model.internal.RankedProvider;
import org.glassfish.jersey.spi.ComponentProvider;

/**
 * Configurator which initializes and register {@link ClientComponentProvider}
 * instances into {@link BootstrapBag}.
 *
 */
public class ClientComponentProviderConfigurator implements BootstrapConfigurator {

    private static final Comparator<RankedProvider<ClientComponentProvider>> RANKED_COMPARATOR
            = new RankedComparator<>(RankedComparator.Order.DESCENDING);

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        // There are situation in which ComponentProviders are not needed therefore their entire initialization is wrapped
        // into a lazy block.
        LazyValue<Collection<ComponentProvider>> componentProviders
                = Values.lazy((Value<Collection<ComponentProvider>>) () -> getRankedComponentProviders()
                .stream()
                .map(RankedProvider::getProvider)
                .peek(provider -> provider.initialize(injectionManager))
                .collect(Collectors.toList()));
        bootstrapBag.setComponentProviders(componentProviders);
    }

    @Override
    public void postInit(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        bootstrapBag.getComponentProviders().get().forEach(ComponentProvider::done);
    }

    private static Collection<RankedProvider<ClientComponentProvider>> getRankedComponentProviders()
            throws ServiceConfigurationError {
        return StreamSupport.stream(ServiceFinder.find(ClientComponentProvider.class).spliterator(), false)
                .map(RankedProvider::new)
                .sorted(RANKED_COMPARATOR)
                .collect(Collectors.toList());
    }
}
