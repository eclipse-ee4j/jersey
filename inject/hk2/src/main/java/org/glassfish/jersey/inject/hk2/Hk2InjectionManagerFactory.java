/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.hk2;

import java.security.AccessController;

import javax.annotation.Priority;

import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerFactory;
import org.glassfish.jersey.internal.util.PropertiesHelper;

/**
 * Factory which is able to create {@link InjectionManager} instance using service loading and automatically initialize injection
 * manager using {@code parent} or immediately registers binder.
 */
@Priority(10)
public class Hk2InjectionManagerFactory implements InjectionManagerFactory {

    /**
     * Hk2 Injection manager strategy.
     * <p>
     * Value can be supplied only via java properties, which would typically be done using '-D' parameter,
     * for example: {@code java -Dorg.glassfish.jersey.hk2.injection.manager.strategy=delayed ...}
     * <p>
     * Valid values are "immediate" and "delayed" and values are case-insensitive.
     * <p>
     * Default value is "immediate".
     */
    public static final String HK2_INJECTION_MANAGER_STRATEGY = "org.glassfish.jersey.hk2.injection.manager.strategy";

    private enum Hk2InjectionManagerStrategy {

        /**
         * @see ImmediateHk2InjectionManager
         */
        IMMEDIATE {
            @Override
            InjectionManager createInjectionManager(final Object parent) {
                return new ImmediateHk2InjectionManager(parent);
            }
        },
        /**
         * @see DelayedHk2InjectionManager
         */
        DELAYED {
            @Override
            InjectionManager createInjectionManager(final Object parent) {
                return new DelayedHk2InjectionManager(parent);
            }
        };

        abstract InjectionManager createInjectionManager(Object parent);
    }

    @Override
    public InjectionManager create(Object parent) {
        return initInjectionManager(getStrategy().createInjectionManager(parent));
    }

    /**
     * Check HK2 Strategy property {@link #HK2_INJECTION_MANAGER_STRATEGY} and returns {@code true} if the current HK2 Strategy is
     * "immediate".
     *
     * @return {@code true} if the current HK2 Strategy is "immediate".
     */
    public static boolean isImmediateStrategy() {
        return getStrategy() == Hk2InjectionManagerStrategy.IMMEDIATE;
    }

    private static Hk2InjectionManagerStrategy getStrategy() {
        String value = AccessController.doPrivileged(PropertiesHelper.getSystemProperty(HK2_INJECTION_MANAGER_STRATEGY));
        if (value == null || value.isEmpty()) {
            return Hk2InjectionManagerStrategy.IMMEDIATE;
        }

        if ("immediate".equalsIgnoreCase(value)) {
            return Hk2InjectionManagerStrategy.IMMEDIATE;
        } else if ("delayed".equalsIgnoreCase(value)) {
            return Hk2InjectionManagerStrategy.DELAYED;
        } else {
            throw new IllegalStateException("Illegal value of " + HK2_INJECTION_MANAGER_STRATEGY
                                            + ". Expected \"immediate\" or \"delayed\", the actual value is: " + value);
        }
    }

    private InjectionManager initInjectionManager(InjectionManager injectionManager) {
        injectionManager.register(Bindings.service(injectionManager).to(InjectionManager.class));
        return injectionManager;
    }
}
