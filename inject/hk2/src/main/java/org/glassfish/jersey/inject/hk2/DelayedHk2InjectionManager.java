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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InstanceBinding;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * Implementation of {@link org.glassfish.jersey.internal.inject.InjectionManager} that is able to delay service's registration
 * and injection to {@link #completeRegistration()} phase. During the Jersey bootstrap just keep the bindings and other
 * operation for a later use.
 *
 * @author Petr Bouda
 */
public class DelayedHk2InjectionManager extends AbstractHk2InjectionManager {

    // Keeps all binders and bindings added to the InjectionManager during the bootstrap.
    private final AbstractBinder bindings = new AbstractBinder() {
        @Override
        protected void configure() {
        }
    };

    // Keeps DI provider specific object for registration.
    private final List<org.glassfish.hk2.utilities.Binder> providers = new ArrayList<>();

    private boolean completed = false;

    /**
     * Constructor with parent.
     *
     * @param parent parent of type {@link org.glassfish.jersey.internal.inject.InjectionManager} or {@link ServiceLocator}.
     */
    DelayedHk2InjectionManager(Object parent) {
        super(parent);
    }

    @Override
    public void register(Binding binding) {
        // TODO: Remove this temporary hack and replace it using different Singleton SubResource/EnhancedSubResource registration.
        // After the completed registration is able to register ClassBinding Singleton and InstanceBinding.
        // Unfortunately, there is no other simple way how to recognize and allow only SubResource registration after the
        // completed registration.
        if (completed && (binding.getScope() == Singleton.class || binding instanceof InstanceBinding)) {
            Hk2Helper.bind(getServiceLocator(), binding);
        } else {
            bindings.bind(binding);
        }
    }

    @Override
    public void register(Iterable<Binding> bindings) {
        for (Binding binding : bindings) {
            this.bindings.bind(binding);
        }
    }

    @Override
    public void register(Binder binder) {
        for (Binding binding : Bindings.getBindings(this, binder)) {
            bindings.bind(binding);
        }
    }

    @Override
    public void register(Object provider) throws IllegalArgumentException {
        if (isRegistrable(provider.getClass())) {
            providers.add((org.glassfish.hk2.utilities.Binder) provider);
        } else {
            throw new IllegalArgumentException(LocalizationMessages.HK_2_PROVIDER_NOT_REGISTRABLE(provider.getClass()));
        }
    }

    @Override
    public void completeRegistration() throws IllegalStateException {
        Hk2Helper.bind(this, bindings);
        ServiceLocatorUtilities.bind(getServiceLocator(), providers.toArray(new org.glassfish.hk2.utilities.Binder[]{}));
        completed = true;
    }
}
