/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Payara Foundation and/or its affiliates. All rights reserved.
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

import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import org.glassfish.jersey.client.spi.ClientComponentProvider;
import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * Jersey CDI integration implementation. Implements
 * {@link ClientComponentProvider Jersey component provider} to serve CDI beans
 * obtained from the actual CDI bean manager. To properly inject JAX-RS/Jersey
 * managed beans into CDI, it also serves as a {@link Extension CDI Extension},
 * that intercepts CDI injection targets.
 *
 * @author Jakub Podlesak
 */
@Priority(200)
public class ClientCdiComponentProvider implements ClientComponentProvider {

    private CdiComponentProvider delegateProvider;

    @Override
    public void initialize(final InjectionManager injectionManager) {
        BeanManager beanManager = CdiUtil.getBeanManager();
        if (beanManager != null) {
            this.delegateProvider = beanManager.getExtension(CdiComponentProvider.class);
            this.delegateProvider.initialize(injectionManager);
        }
    }

    @Override
    public boolean bind(final Class<?> clazz, final Set<Class<?>> providerContracts) {
        if (this.delegateProvider != null && this.delegateProvider.isCdiComponent(clazz)) {
            return this.delegateProvider.bind(clazz, providerContracts);
        } else {
            return false;
        }
    }

    @Override
    public void done() {
        if (this.delegateProvider != null) {
            this.delegateProvider.done();
        }
    }

}
