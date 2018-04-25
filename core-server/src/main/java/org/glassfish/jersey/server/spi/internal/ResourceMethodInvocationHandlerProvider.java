/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spi.internal;

import java.lang.reflect.InvocationHandler;

import org.glassfish.jersey.server.model.Invocable;

/**
 * Provides the {@link InvocationHandler invocation handler} instances designated
 * to handle invocations of the supplied {@link Invocable invocable resource methods}.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface ResourceMethodInvocationHandlerProvider {

    /**
     * Get the invocation handler for the invocable resource method. May return
     * {@code null} in case the method is not supported by the provider for whatever
     * reason.
     *
     * @param method invocable resource method.
     * @return invocation handler for the invocable resource method.
     */
    public InvocationHandler create(Invocable method);
}
