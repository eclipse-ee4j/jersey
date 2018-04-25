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

package org.glassfish.jersey.ext.cdi1x.internal.spi;

import javax.enterprise.inject.spi.BeanManager;

/**
 * Provider SPI for CDI {@link javax.enterprise.inject.spi.BeanManager} for the current context.
 * Implementations can decide how to obtain bean manager (e.g. {@link javax.naming.InitialContext}, CDI 1.1 API, ...).
 *
 * @author Michal Gajdos
 * @since 2.17
 */
public interface BeanManagerProvider {

    /**
     * Get the CDI {@link javax.enterprise.inject.spi.BeanManager bean manager} for the current context.
     *
     * @return bean manager for the current context or {@code null} if no bean manager is available.
     */
    public BeanManager getBeanManager();
}
