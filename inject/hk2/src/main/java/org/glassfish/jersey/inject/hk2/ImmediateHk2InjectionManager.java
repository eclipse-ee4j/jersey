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

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * Implementation of {@link org.glassfish.jersey.internal.inject.InjectionManager} that is able to register and inject services.
 *
 * @author Petr Bouda
 */
public class ImmediateHk2InjectionManager extends AbstractHk2InjectionManager {

    /**
     * Constructor with parent.
     *
     * @param parent parent of type {@link org.glassfish.jersey.internal.inject.InjectionManager} or {@link ServiceLocator}.
     */
    ImmediateHk2InjectionManager(Object parent) {
        super(parent);
    }

    @Override
    public void completeRegistration() throws IllegalStateException {
        // No-op method.
    }

    @Override
    public void register(Binding binding) {
        Hk2Helper.bind(getServiceLocator(), binding);
    }

    @Override
    public void register(Iterable<Binding> descriptors) {
        Hk2Helper.bind(getServiceLocator(), descriptors);
    }

    @Override
    public void register(Binder binder) {
        Hk2Helper.bind(this, binder);
    }

    @Override
    public void register(Object provider) {
        if (isRegistrable(provider.getClass())) {
            ServiceLocatorUtilities.bind(getServiceLocator(), (org.glassfish.hk2.utilities.Binder) provider);
        } else {
            throw new IllegalArgumentException(LocalizationMessages.HK_2_PROVIDER_NOT_REGISTRABLE(provider.getClass()));
        }
    }
}
