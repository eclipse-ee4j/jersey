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

package org.glassfish.jersey.internal.inject;

import java.util.Set;

/**
 * Internal service to help determine
 * which HK2 factory provided components should be treated
 * as request scoped. This is to help avoid having
 * dynamic proxies of request scoped components injected into
 * factory created components managed by 3rd party component
 * providers
 *
 * Jakub Podlesak (jakub.podlesak at oracle.com).
 */
public interface ForeignRequestScopeBridge {

    /**
     * Get me a set of classes that are managed outside of HK2
     * and should be treated as if HK2 request scoped.
     *
     * @return set of classes representing foreign request scoped components.
     */
    Set<Class<?>> getRequestScopedComponents();
}
