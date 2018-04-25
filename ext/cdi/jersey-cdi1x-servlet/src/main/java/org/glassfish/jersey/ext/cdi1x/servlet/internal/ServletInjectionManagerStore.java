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

package org.glassfish.jersey.ext.cdi1x.servlet.internal;

import org.glassfish.jersey.ext.cdi1x.internal.GenericInjectionManagerStore;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerStore;
import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * {@link InjectionManagerStore injection manager} for servlet based containers. The provider
 * enables WAR and EAR to be deployed on a servlet container and be properly injected.
 *
 * @author Michal Gajdos
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @since 2.17
 */
public class ServletInjectionManagerStore extends GenericInjectionManagerStore {

    @Override
    public InjectionManager lookupInjectionManager() {
        return CdiExternalRequestScope.actualInjectionManager.get();
    }
}
