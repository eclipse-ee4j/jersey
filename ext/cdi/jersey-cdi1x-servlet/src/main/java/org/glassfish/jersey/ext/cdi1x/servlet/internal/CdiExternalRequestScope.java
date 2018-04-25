/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.enterprise.context.ApplicationScoped;

import org.glassfish.jersey.ext.cdi1x.internal.JerseyVetoed;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ExternalRequestContext;
import org.glassfish.jersey.server.spi.ExternalRequestScope;

/**
 * Weld specific request scope to align CDI request context with Jersey.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@ApplicationScoped
@JerseyVetoed
public class CdiExternalRequestScope implements ExternalRequestScope<Object> {

    public static final ThreadLocal<InjectionManager> actualInjectionManager = new ThreadLocal<>();

    @Override
    public ExternalRequestContext<Object> open(InjectionManager injectionManager) {
        actualInjectionManager.set(injectionManager);
        return new ExternalRequestContext<>(null);
    }

    @Override
    public void resume(final ExternalRequestContext<Object> ctx, InjectionManager injectionManager) {
        actualInjectionManager.set(injectionManager);
    }

    @Override
    public void suspend(final ExternalRequestContext<Object> ctx, InjectionManager injectionManager) {
        actualInjectionManager.remove();
    }

    @Override
    public void close() {
        actualInjectionManager.remove();
    }
}
