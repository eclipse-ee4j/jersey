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

package org.glassfish.jersey.server;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.model.ContractProvider;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.wadl.processor.OptionsMethodProcessor;

/**
 * Configurator which initializes and register {@link ModelProcessor} instances into {@link BootstrapBag}.
 *
 * @author Petr Bouda
 */
class ModelProcessorConfigurator implements BootstrapConfigurator {

    private static final Function<Object, ModelProcessor> CAST_TO_MODEL_PROCESSOR = ModelProcessor.class::cast;

    private static final Predicate<Binding> BINDING_MODEL_PROCESSOR_ONLY =
            binding -> binding.getContracts().contains(ModelProcessor.class);

    private static final Predicate<ContractProvider> CONTRACT_PROVIDER_MODEL_PROCESSOR_ONLY =
            provider -> provider.getContracts().contains(ModelProcessor.class);

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        ServerBootstrapBag serverBag = (ServerBootstrapBag) bootstrapBag;
        ResourceConfig runtimeConfig = serverBag.getRuntimeConfig();
        ComponentBag componentBag = runtimeConfig.getComponentBag();

        OptionsMethodProcessor optionsMethodProcessor = new OptionsMethodProcessor();
        injectionManager.register(Bindings.service(optionsMethodProcessor).to(ModelProcessor.class));

        // Get all model processors, registered as an instance or class
        List<ModelProcessor> modelProcessors =
                Stream.concat(
                        componentBag.getClasses(CONTRACT_PROVIDER_MODEL_PROCESSOR_ONLY).stream()
                                .map(injectionManager::createAndInitialize),
                        componentBag.getInstances(CONTRACT_PROVIDER_MODEL_PROCESSOR_ONLY).stream())
                        .map(CAST_TO_MODEL_PROCESSOR)
                        .collect(Collectors.toList());
        modelProcessors.add(optionsMethodProcessor);

        // model processors registered using binders
        List<ModelProcessor> modelProcessorsFromBinders = ComponentBag
                .getFromBinders(injectionManager, componentBag, CAST_TO_MODEL_PROCESSOR, BINDING_MODEL_PROCESSOR_ONLY);
        modelProcessors.addAll(modelProcessorsFromBinders);

        serverBag.setModelProcessors(modelProcessors);
    }
}
