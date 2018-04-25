/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.gf.ejb.internal;

import javax.annotation.PostConstruct;
import javax.interceptor.InvocationContext;

import org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider;
import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * EJB interceptor to inject Jersey specific stuff into EJB beans.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public final class EjbComponentInterceptor {

    private final InjectionManager injectionManager;

    /**
     * Create new EJB component injection manager.
     *
     * @param injectionManager injection manager.
     */
    public EjbComponentInterceptor(final InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
    }

    @PostConstruct
    private void inject(final InvocationContext context) throws Exception {

        final Object beanInstance = context.getTarget();
        injectionManager.inject(beanInstance, CdiComponentProvider.CDI_CLASS_ANALYZER);

        // Invoke next interceptor in chain
        context.proceed();
    }
}
