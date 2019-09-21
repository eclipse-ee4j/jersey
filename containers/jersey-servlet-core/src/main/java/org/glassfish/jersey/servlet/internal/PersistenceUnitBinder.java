/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.servlet.internal;

import java.lang.reflect.Proxy;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.GenericType;

import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletConfig;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.server.ContainerException;

/**
 * {@link PersistenceUnit Persistence unit} injection binder.
 *
 * @author Michal Gajdos
 */
public class PersistenceUnitBinder extends AbstractBinder {

    private final ServletConfig servletConfig;

    /**
     * Prefix of the persistence unit init param.
     */
    public static final String PERSISTENCE_UNIT_PREFIX = "unit:";

    /**
     * Creates a new binder for {@link PersistenceUnitInjectionResolver}.
     *
     * @param servletConfig servlet config to find persistence units.
     */
    public PersistenceUnitBinder(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    @Singleton
    private static class PersistenceUnitInjectionResolver implements InjectionResolver<PersistenceUnit> {

        private final Map<String, String> persistenceUnits = new HashMap<>();

        private PersistenceUnitInjectionResolver(ServletConfig servletConfig) {
            for (final Enumeration parameterNames = servletConfig.getInitParameterNames(); parameterNames.hasMoreElements(); ) {
                final String key = (String) parameterNames.nextElement();

                if (key.startsWith(PERSISTENCE_UNIT_PREFIX)) {
                    persistenceUnits.put(key.substring(PERSISTENCE_UNIT_PREFIX.length()),
                            "java:comp/env/" + servletConfig.getInitParameter(key));
                }
            }
        }

        @Override
        public Object resolve(Injectee injectee) {
            if (!injectee.getRequiredType().equals(EntityManagerFactory.class)) {
                return null;
            }

            final PersistenceUnit annotation = injectee.getParent().getAnnotation(PersistenceUnit.class);
            final String unitName = annotation.unitName();

            if (!persistenceUnits.containsKey(unitName)) {
                throw new ContainerException(LocalizationMessages.PERSISTENCE_UNIT_NOT_CONFIGURED(unitName));
            }

            return Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class[] {EntityManagerFactory.class},
                    new ThreadLocalNamedInvoker<EntityManagerFactory>(persistenceUnits.get(unitName)));
        }

        @Override
        public boolean isConstructorParameterIndicator() {
            return false;
        }

        @Override
        public boolean isMethodParameterIndicator() {
            return false;
        }

        @Override
        public Class<PersistenceUnit> getAnnotation() {
            return PersistenceUnit.class;
        }
    }

    @Override
    protected void configure() {
        bind(new PersistenceUnitInjectionResolver(servletConfig))
                .to(new GenericType<InjectionResolver<PersistenceUnit>>() {});
    }
}
