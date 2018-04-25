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

package org.glassfish.jersey.ext.cdi1x.spi;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * Helper SPI to help specify Jersey HK2 custom bound types that should
 * be HK2-injectable into CDI components.
 *
 * <p>Implementation of this type must be registered via META-INF/services
 * mechanism. I.e. fully qualified name of an implementation class
 * must be written into <code>META-INF/services/org.glassfish.jersey.ext.cdi11.spi.Hk2CustomBoundTypesProvider</code>
 * file.
 *
 * <p>If more than one implementation is found, only a single one is selected that has the highest priority.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public interface Hk2CustomBoundTypesProvider {

    /**
     * Provide a set of types that should became accessible
     * by CDI container in a form of CDI beans backed by HK2.
     *
     * <p>Jersey will ask CDI container to veto these types
     * and will register HK2 backed beans into CDI, so that @{@link javax.inject.Inject}
     * marked injection points could be satisfied.
     *
     * <p>The end user is responsible for defining necessary HK2 bindings
     * within Jersey application. Should any of such bindings remain
     * undefined, runtime errors are likely to occur.
     *
     * @return set of types for which HK2 backed CDI beans shall be registered.
     */
    public Set<Type> getHk2Types();
}
