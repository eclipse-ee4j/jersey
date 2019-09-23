/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.weld.se;

import org.glassfish.jersey.ext.cdi1x.internal.GenericInjectionManagerStore;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerStore;
import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * {@link InjectionManagerStore Injection manager} for Weld SE container. The provider
 * enables multiple Jersey applications to be deployed within a single HTTP container.
 *
 * @author Jakub Podlesak
 * @since 2.20
 */
public class WeldInjectionManagerStore extends GenericInjectionManagerStore {

    @Override
    public InjectionManager lookupInjectionManager() {
        return WeldRequestScope.actualInjectorManager.get();
    }
}
