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

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.glassfish.jersey.spi.Contract;

/**
 * Interceptor for method validation processing.
 *
 * Allows to override the default Jersey behaviour. By default, the Jersey runtime throws a
 * {@link javax.validation.ValidationException} or one of its subclasses,
 * that gets mapped to a HTTP 400 response, if any validation issues occur. In such case
 * the actual resource method does not get invoked at all.
 * <p>
 * Validation interceptor implementation allows to e.g. swallow the {@link ConstraintViolationException}
 * and handle the validation issues in the resource method. It is also possible to tweak
 * validated components. This could be utilized in case of proxied resources,
 * when field validation is not possible on a dynamic proxy, and the validator requires
 * the original delegated instance.
 * </p>
 * <p>
 * Each validation interceptor implementation must invoke proceed
 * method on provided interceptor context as part of interception processing.
 * </p>
 *
 * @author Jakub Podlesak (jakub.podleak at oracle.com)
 * @since 2.18
 */
@Contract
public interface ValidationInterceptor {

    /**
     * Used to intercept resource method validation processing.
     * <p/>
     * To allow further validation processing, every and each implementation
     * must invoke {@link ValidationInterceptorContext#proceed()} method.
     *
     * @param context method validation context.
     * @throws ValidationException in case the validation exception should be thrown as a result of the validation processing.
     */
    public void onValidate(ValidationInterceptorContext context) throws ValidationException;
}
