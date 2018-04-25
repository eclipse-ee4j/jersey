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

/**
 * An extension interface for implementations of {@link InjectionManagerStore}. HK2 locator
 * managers implementing this interface are notified when an {@link javax.enterprise.inject.spi.InjectionTarget injection target}
 * is processed by {@link org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider}. Locator managers can then set the
 * effective injection manager to the processed {@link InjectionManagerInjectedTarget target}.
 *
 * @author Michal Gajdos
 */
public interface InjectionTargetListener {

    /**
     * Notify the HK2 locator manager about new injection target being processed.
     *
     * @param target processed injection target.
     */
    public void notify(final InjectionManagerInjectedTarget target);
}
