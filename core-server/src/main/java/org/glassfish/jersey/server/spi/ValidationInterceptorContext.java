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

package org.glassfish.jersey.server.spi;

import javax.validation.ValidationException;

import org.glassfish.jersey.server.model.Invocable;

/**
 * Context for resource method validation interception processing (see {@link ValidationInterceptor}).
 * The context gives access to key validation data.
 * It also allows interceptor implementation to tweak resource and/or parameters that are going to be validated.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @see ValidationInterceptor
 * @since 2.18
 */
public interface ValidationInterceptorContext {

    /**
     * Provide actual resource instance that will get validated.
     *
     * @return current resource instance.
     */
    public Object getResource();

    /**
     * Setter for resource instance that should get validated.
     *
     * @param resource instance to validate
     */
    public void setResource(Object resource);

    /**
     * Provide invocable for which validation will been done.
     *
     * @return actual invocable instance.
     */
    public Invocable getInvocable();

    /**
     * Provide method parameters for which validation will be done.
     *
     * @return actual method parameters.
     */
    public Object[] getArgs();

    /**
     * Method parameters setter.
     *
     * @param args method parameters to be used for validation.
     */
    public void setArgs(Object[] args);

    /**
     * Proceed with validation.
     *
     * This method must be invoked by a validation interceptor implementation.
     *
     * @throws javax.validation.ValidationException in case the further validation processing failed with a validation error.
     */
    public void proceed() throws ValidationException;
}
