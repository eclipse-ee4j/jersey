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

package org.glassfish.jersey.ext.cdi1x.transaction.internal;

import java.io.Serializable;

import javax.ws.rs.WebApplicationException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.Transactional;

import org.glassfish.jersey.ext.cdi1x.internal.JerseyVetoed;

/**
 * Transactional interceptor to help retain {@link WebApplicationException}
 * thrown by transactional beans.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Priority(value = Interceptor.Priority.PLATFORM_BEFORE + 199)
@Interceptor
@Transactional
@JerseyVetoed
public final class WebAppExceptionInterceptor implements Serializable {

    private static final long serialVersionUID = -1L;

    @Inject
    @TransactionalExceptionInterceptorProvider.WaeQualifier
    private WebAppExceptionHolder store;

    @AroundInvoke
    public Object intercept(final InvocationContext ic) throws Exception {
        try {
            return ic.proceed();
        } catch (final WebApplicationException wae) {
            if (store != null) {
                store.setException(wae);
            }
            throw wae;
        }
    }
}
