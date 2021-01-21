/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.model.ContractProvider;
import org.glassfish.jersey.server.spi.ComponentProvider;

import javax.enterprise.inject.spi.BeanManager;
import java.util.Set;

/**
 * Implementation of ComponentProvider for Jersey Server
 */
public class CdiServerComponentProvider implements ComponentProvider {
    private CdiComponentProvider componentProvider;

    @Override
    public void initialize(InjectionManager injectionManager) {
        BeanManager beanManager = CdiUtil.getBeanManager();

        if (beanManager != null) {
            // Try to get CdiComponentProvider created by CDI.
            componentProvider = beanManager.getExtension(CdiComponentProvider.class);
            componentProvider.initialize(injectionManager);
        }
    }

    @Override
    public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {
        return componentProvider != null ? componentProvider.bind(component, providerContracts) : false;
    }

    @Override
    public boolean bind(Class<?> component, ContractProvider contractProvider) {
        return componentProvider != null ? componentProvider.bind(component, contractProvider) : false;
    }

    @Override
    public void done() {
        if (componentProvider != null) {
            componentProvider.done();
        }
    }
}
