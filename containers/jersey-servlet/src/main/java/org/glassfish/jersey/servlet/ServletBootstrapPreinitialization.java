/*
 * Copyright (c) 2024, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.servlet;

import jakarta.inject.Singleton;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.RuntimeType;
import org.glassfish.jersey.innate.BootstrapPreinitialization;
import org.glassfish.jersey.innate.inject.InjectionIds;
import org.glassfish.jersey.innate.inject.InternalBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.servlet.internal.PersistenceUnitBinder;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ServletBootstrapPreinitialization implements BootstrapPreinitialization {
    @Override
    public void preregister(RuntimeType runtimeType, InjectionManager injectionManager) {
        if (RuntimeType.SERVER == runtimeType) {
            injectionManager.register(
                    new WebComponent.WebComponentBinder(Collections.emptyMap(), new BootstrapWebConfig(), false));
            injectionManager.register(new InternalBinder() {
                @Override
                protected void configure() {
                    bindFactory(() -> (FilterConfig) null).to(FilterConfig.class).in(Singleton.class)
                            .id(InjectionIds.SERVLET_FILTER_CONFIG.id());
                }
            });
        }
    }

    private static class BootstrapWebConfig implements WebConfig {

        @Override
        public ConfigType getConfigType() {
            return ConfigType.ServletConfig;
        }

        @Override
        public ServletConfig getServletConfig() {
            @SuppressWarnings("removal")
            ClassLoader cl = java.security.AccessController.doPrivileged(ReflectionHelper.getClassLoaderPA(ServletContext.class));
            return (ServletConfig) Proxy.newProxyInstance(cl, new Class[]{ServletConfig.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getInitParameterNames":
                        return BootstrapWebConfig.this.getInitParameterNames();
                }
                return null;
            });
        }

        @Override
        public FilterConfig getFilterConfig() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @Override
        public Enumeration getInitParameterNames() {
            return Collections.enumeration(WebComponent.isJPA()
                    ? Collections.emptyList() : List.of(PersistenceUnitBinder.PERSISTENCE_UNIT_PREFIX));
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }
    }
}
