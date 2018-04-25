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

package org.glassfish.jersey.server.validation.internal;

import java.util.Iterator;

import javax.validation.ValidationException;

import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.ValidationInterceptor;
import org.glassfish.jersey.server.spi.ValidationInterceptorContext;

/**
 * Validation executor for resource method validation processing. It is intended for a one-off usage
 * when the executor instance serves also as a {@link org.glassfish.jersey.server.spi.ValidationInterceptorContext}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
final class ValidationInterceptorExecutor implements ValidationInterceptorContext {

    private Object resource;
    private Object[] args;
    private final Invocable invocable;

    private final Iterator<ValidationInterceptor> iterator;

    /**
     * Create a one-off validation executor for given resource, invocable and parameter and given
     * interceptors.
     *
     * @param resource  actual resource instance to get validated
     * @param invocable resource method
     * @param args      actual resource method parameters
     * @param iterator  validator interceptors to be involved
     */
    public ValidationInterceptorExecutor(
            final Object resource,
            final Invocable invocable,
            final Object[] args,
            final Iterator<ValidationInterceptor> iterator) {

        this.resource = resource;
        this.invocable = invocable;
        this.args = args;
        this.iterator = iterator;
    }

    @Override
    public Object getResource() {
        return resource;
    }

    @Override
    public void setResource(final Object resource) {
        this.resource = resource;
    }

    @Override
    public Invocable getInvocable() {
        return invocable;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public void setArgs(final Object[] args) {
        this.args = args;
    }

    @Override
    public void proceed() throws ValidationException {
        final ValidationInterceptor validationInterceptor = iterator.next();
        validationInterceptor.onValidate(this);
    }
}
