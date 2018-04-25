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

package org.glassfish.jersey.weld.se;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.glassfish.jersey.ext.cdi1x.internal.JerseyVetoed;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ExternalRequestContext;
import org.glassfish.jersey.server.spi.ExternalRequestScope;

import org.jboss.weld.context.bound.BoundRequestContext;

/**
 * Weld specific request scope to align CDI request context with Jersey.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@ApplicationScoped
@JerseyVetoed
public class WeldRequestScope implements ExternalRequestScope<Map<String, Object>> {

    @Inject
    private BoundRequestContext context;

    private final ThreadLocal<Map<String, Object>> actualMap = new ThreadLocal<>();

    public static final ThreadLocal<InjectionManager> actualInjectorManager = new ThreadLocal<>();

    @Override
    public ExternalRequestContext<Map<String, Object>> open(InjectionManager injectionManager) {
        final Map<String, Object> newMap = new ConcurrentHashMap<>();
        actualMap.set(newMap);
        context.associate(newMap);
        context.activate();
        actualInjectorManager.set(injectionManager);
        return new ExternalRequestContext<>(newMap);
    }

    @Override
    public void resume(final ExternalRequestContext<Map<String, Object>> ctx, InjectionManager injectionManager) {
        final Map<String, Object> newMap = ctx.getContext();
        actualInjectorManager.set(injectionManager);
        actualMap.set(newMap);
        context.associate(newMap);
        context.activate();
    }

    @Override
    public void suspend(final ExternalRequestContext<Map<String, Object>> ctx, InjectionManager injectionManager) {
        try {
            final Map<String, Object> contextMap = actualMap.get();
            if (contextMap != null) {
                context.deactivate();
                context.dissociate(contextMap);
            }
        } finally {
            actualMap.remove();
            actualInjectorManager.remove();
        }
    }

    @Override
    public void close() {
        try {
            final Map<String, Object> contextMap = actualMap.get();
            if (contextMap != null) {
                context.invalidate();
                context.deactivate();
                context.dissociate(contextMap);
            } else {
                context.deactivate();
            }
        } finally {
            actualMap.remove();
            actualInjectorManager.remove();
        }
    }
}
