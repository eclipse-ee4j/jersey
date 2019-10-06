/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.spi;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.model.ContractProvider;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Factory class creating instances of {@link BinderConfiguration} classes used to configure Binders registered
 * to a configuration. While it is created for a backward compatibility with HK2 {@code AbstractBinder} to be
 * able to be registered to a configuration (while HK2 injection module is on the classpath), a factory
 * implementing this interface is used for registering Jersey {@code AbstractBinder}, too.
 *
 * @since 2.29
 */
public interface BinderConfigurationFactory {

    /**
     * Creates a {@link BinderConfiguration} instance that has a reference to {@code getInstances} function returning all
     * registered instances filtered by provided {@link Predicate<ContractProvider>} that can be further traversed and configured.
     *
     * @param getInstances a function returning filtered instances registered to a configuration. The
     * {@link Predicate<ContractProvider>} is used to filter out all the instances not to be further processed by the
     * {@link BinderConfiguration}.
     * @return {@link BinderConfiguration} instance used to register/configure the provided filtered instances.
     */
    BinderConfiguration createBinderConfiguration(Function<Predicate<ContractProvider>, Set<Object>> getInstances);

    /**
     * This configuration object configure all the instances provided by the {@code getInstances} function passed from the
     * factory {@link BinderConfigurationFactory#createBinderConfiguration(Function)} method.
     * <p>
     * The implementation possibly can hold a list of already configured {@code Binders} so that consecutive calls do
     * not register the already registered {@code Binders} again.
     */
    interface BinderConfiguration {
        /**
         * The provided {@code getInstances} function is used to get registered (filtered) instances in a
         * {@link jakarta.ws.rs.core.Configuration}
         *
         * @param injectionManager {@link InjectionManager} to be used to configure the {@code Binder}
         * @return {@code true} if a {@code Binder} has been configured.
         */
        boolean configureBinders(InjectionManager injectionManager);
    }
}
