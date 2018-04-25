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

/**
 * Supplier to provide CDI managed components where
 * there is no clear mapping between the CDI and scopes.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Vetoed
public final class GenericCdiBeanSupplier extends AbstractCdiBeanSupplier {

    public GenericCdiBeanSupplier(Class rawType,
                                    InjectionManager injectionManager,
                                    BeanManager beanManager,
                                    boolean cdiManaged) {
        super(rawType, injectionManager, beanManager, cdiManaged);
    }

    @Override
    public Object get() {
        return _provide();
    }
}
