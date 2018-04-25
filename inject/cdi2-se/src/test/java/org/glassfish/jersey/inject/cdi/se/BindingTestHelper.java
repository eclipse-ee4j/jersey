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

package org.glassfish.jersey.inject.cdi.se;

import java.util.function.Consumer;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;

/**
 * Helper class to minimize the code in tested classes.
 *
 * @author Petr Bouda
 */
class BindingTestHelper {

    /**
     * Accepts the provided consumer to created and register the binder.
     *
     * @param injectionManager injection manager which accepts the consumer.
     * @param bindConsumer     consumer to populate a binder.
     */
    static void bind(InjectionManager injectionManager, Consumer<AbstractBinder> bindConsumer) {
        AbstractBinder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                bindConsumer.accept(this);
            }
        };

        injectionManager.register(binder);
        injectionManager.completeRegistration();
    }

    /**
     * Creates a new {@link InjectionManager}.
     *
     * @return newly created {@code InjectionManager}.
     */
    static InjectionManager createInjectionManager() {
        return Injections.createInjectionManager();
    }
}
