/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.spi;

import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.Beta;
import org.glassfish.jersey.internal.inject.AbstractBinder;

/**
 * <p>
 *     The entry point for pre-initialize Jersey during bootstrap. Register the beans that are not recognized by the injection
 *     framework to be injected in runtime. Register beans for the specific runtime type into the {@link AbstractBinder}.
 * </p>
 */
@Beta
public interface BootstrapPreinitialization {
    /**
     * Manually register beans that are not automatically recognised by the injection framework.
     * @param runtimeType
     * @param binder
     */
    void register(RuntimeType runtimeType, AbstractBinder binder);
}
