/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.inject.internal;

import org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider;
import org.glassfish.jersey.internal.inject.InjectionManager;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A CDI producer producing servlet beans in a {@code RequestScope}.
 */
class ServletReferenceProducer {
    @Inject
    InjectionManager injectionManager;

    @Inject
    BeanManager beanManager;

    @Produces
    @JerseyContext
    @RequestScoped
    public HttpServletRequest produceHttpServletRequest() {
        return injectionManager().getInstance(HttpServletRequest.class);
    }

    @Produces
    @JerseyContext
    @RequestScoped
    public HttpServletResponse produceHttpServletResponse() {
        return injectionManager().getInstance(HttpServletResponse.class);
    }

    @Produces
    @JerseyContext
    @RequestScoped
    public ServletContext produceServletContext() {
        return injectionManager().getInstance(ServletContext.class);
    }

    @Produces
    @JerseyContext
    @RequestScoped
    public ServletConfig produceServletConfig() {
        return injectionManager().getInstance(ServletConfig.class);
    }

    @Produces
    @JerseyContext
    @RequestScoped
    public FilterConfig produceFilterConfig() {
        return injectionManager().getInstance(FilterConfig.class);
    }

    private InjectionManager injectionManager() {
        InjectionManager injectionManager = beanManager.getExtension(CdiComponentProvider.class).getEffectiveInjectionManager();
        if (injectionManager != null && !injectionManager.isShutdown()) {
            return injectionManager;
        }
        return this.injectionManager;
    }
}
