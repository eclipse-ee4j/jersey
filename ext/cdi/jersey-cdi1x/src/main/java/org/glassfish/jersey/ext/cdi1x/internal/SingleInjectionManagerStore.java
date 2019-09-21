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

import javax.ws.rs.WebApplicationException;

import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerStore;
import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * Default {@link InjectionManagerStore injection manager} that assumes only one
 * {@link InjectionManager injection manager} per application is used.
 *
 * @author Michal Gajdos
 * @since 2.17
 */
final class SingleInjectionManagerStore implements InjectionManagerStore {

    private volatile InjectionManager injectionManager;

    @Override
    public void registerInjectionManager(final InjectionManager injectionManager) {
        if (this.injectionManager == null) {
            this.injectionManager = injectionManager;
        } else if (this.injectionManager != injectionManager) {
            throw new WebApplicationException(LocalizationMessages.CDI_MULTIPLE_LOCATORS_INTO_SIMPLE_APP());
        }
    }

    @Override
    public InjectionManager getEffectiveInjectionManager() {
        return injectionManager;
    }
}
