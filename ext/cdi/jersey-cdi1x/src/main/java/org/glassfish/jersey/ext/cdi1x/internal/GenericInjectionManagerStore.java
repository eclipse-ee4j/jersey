/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.internal;

import java.util.LinkedList;
import java.util.List;

import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerInjectedTarget;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerStore;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionTargetListener;
import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * Generic {@link InjectionManagerStore injection manager store} that allows multiple
 * injection managers to run in parallel. {@link #lookupInjectionManager()}
 * method must be implemented that shall be utilized at runtime in the case that more than a single
 * injection manager has been registered.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @since 2.20
 */
public abstract class GenericInjectionManagerStore implements InjectionManagerStore, InjectionTargetListener {

    private final List<InjectionManagerInjectedTarget> injectionTargets;

    private volatile InjectionManager injectionManager;

    private volatile boolean multipleInjectionManagers = false;

    public GenericInjectionManagerStore() {
        injectionTargets = new LinkedList<>();
    }

    @Override
    public void registerInjectionManager(final InjectionManager injectionManager) {
        if (!multipleInjectionManagers) {
            if (this.injectionManager == null) { // first one
                this.injectionManager = injectionManager;
            } else { // second one
                this.injectionManager = null;
                multipleInjectionManagers = true;
            } // first and second case
        }

        // pass the injection manager to registered injection targets anyway
        for (final InjectionManagerInjectedTarget target : injectionTargets) {
            target.setInjectionManager(injectionManager);
        }
    }

    @Override
    public InjectionManager getEffectiveInjectionManager() {
        return !multipleInjectionManagers ? injectionManager : lookupInjectionManager();
    }

    /**
     * CDI container specific method to obtain the actual injection manager
     * belonging to the Jersey application where the current HTTP requests
     * is being processed.
     *
     * @return actual injection manager.
     */
    public abstract InjectionManager lookupInjectionManager();

    @Override
    public void notify(final InjectionManagerInjectedTarget target) {

        injectionTargets.add(target);
    }
}
