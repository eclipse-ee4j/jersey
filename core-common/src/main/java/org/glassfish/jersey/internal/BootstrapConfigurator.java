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

package org.glassfish.jersey.internal;

import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * Configurator which contains two methods, {@link #init(InjectionManager, BootstrapBag)} contains {@link InjectionManager}
 * into which only registering services make sense because injection manager has not been completed yet and
 * {@link #postInit(InjectionManager, BootstrapBag)} in which {@link InjectionManager} has been already completed and is able to
 * create and provide services.
 * <p>
 * The configurators should register instances into {@link InjectionManager} only if the instance must be really injectable if
 * the instance can be used internally without the injection, then extend {@link BootstrapBag} and propagate the instance to
 * correct services using constructors or methods in a phase of Jersey initialization.
 *
 * @author Petr Bouda
 */
public interface BootstrapConfigurator {

    /**
     * Pre-initialization method should only register services into {@link InjectionManager} and populate {@link BootstrapBag}.
     *
     * @param injectionManager not completed injection manager.
     * @param bootstrapBag     bootstrap bag with services used in following processing.
     */
    void init(InjectionManager injectionManager, BootstrapBag bootstrapBag);

    /**
     * Post-initialization method can get services from {@link InjectionManager} and is not able to register the new one because
     * injection manager is already completed.
     *
     * @param injectionManager already completed injection manager.
     * @param bootstrapBag     bootstrap bag with services used in following processing.
     */
    default void postInit(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
    }

}
