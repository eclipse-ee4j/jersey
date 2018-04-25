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

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.BeanManager;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * Supplier to provide CDI managed components
 * that should be mapped into Jersey request scope.
 * For these components, Jersey will avoid
 * injecting dynamic proxies for JAX-RS request scoped injectees.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Vetoed
public final class RequestScopedCdiBeanSupplier extends AbstractCdiBeanSupplier {

    public RequestScopedCdiBeanSupplier(Class rawType,
                                          InjectionManager locator,
                                          BeanManager beanManager,
                                          boolean cdiManaged) {
        super(rawType, locator, beanManager, cdiManaged);
    }

    @Override
    @RequestScoped
    public Object get() {
        return _provide();
    }
}
