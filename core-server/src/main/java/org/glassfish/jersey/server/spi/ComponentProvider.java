/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spi;

import java.util.Set;

import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * Component provider interface to allow custom management of 3rd party
 * components life-cycle and dependency injection.
 * <p />
 * An implementation (a component-provider) identifies itself by placing a provider-configuration
 * file (if not already present), {@code org.glassfish.jersey.server.spi.ComponentProvider}
 * in the resource directory <tt>META-INF/services</tt>, and adding the fully
 * qualified service-provider-class of the implementation in the file.
 *
 * Jersey will not even try to inject component provider instances with Jersey artifacts.
 * The SPI providers should be designed so that no dependency injection is needed at the bind time phase.

 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public interface ComponentProvider {


    /**
     * Initializes the component provider with a reference to a injection manager
     * instance, which will get used in the application to manage individual components.
     * Providers should keep a reference to the injection manager for later use.
     * This method will be invoked prior to any bind method calls.
     * The injection manager parameter will not be fully initialized at the time of invocation
     * and should be used as a reference only.
     *
     * @param injectionManager an injection manager.
     */
    void initialize(final InjectionManager injectionManager);

    /**
     * Jersey will invoke this method before binding of each component class internally
     * during initialization of it's injection manager.
     *
     * If the component provider wants to bind the component class
     * itself, it must do so and return true. In that case, Jersey will not
     * bind the component and rely on the component provider in this regard.
     *
     * @param component a component (resource/provider) class.
     * @param providerContracts provider contracts implemented by given component.
     * @return true if the component class has been bound by the provider, false otherwise
     */
    boolean bind(final Class<?> component, Set<Class<?>> providerContracts);

    /**
     * Jersey will invoke this method after all component classes have been bound.
     *
     * If the component provider wants to do some actions after it has seen all component classes
     * registered with the application, this is the right place for the corresponding code.
     */
    void done();
}
