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

package org.glassfish.jersey.tests.cdi.bv;

import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.glassfish.jersey.ext.cdi1x.internal.CdiUtil;
import org.glassfish.jersey.ext.cdi1x.internal.GenericCdiBeanSupplier;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ComponentProvider;

/**
 * Utility that binds HK2 factory to provide CDI managed validation result bean.
 * This is to make sure validation result could be injected as a resource method parameter
 * even when running in CDI environment.
 */
public class CdiValidationResultBinder implements Extension, ComponentProvider {

    @Inject
    BeanManager beanManager;

    InjectionManager injectionManager;

    @Override
    public void initialize(InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
        this.beanManager = CdiUtil.getBeanManager();
    }

    @Override
    public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void done() {
        if (beanManager != null) { // in CDI environment
            Binding binding = Bindings
                    .supplier(new GenericCdiBeanSupplier(
                            CdiValidationResult.class, injectionManager, beanManager, true))
                    .to(CdiValidationResult.class)
                    .to(ValidationResult.class);

            injectionManager.register(binding);
        }
    }
}
