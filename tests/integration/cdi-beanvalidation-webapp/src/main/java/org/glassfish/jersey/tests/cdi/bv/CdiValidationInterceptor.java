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

package org.glassfish.jersey.tests.cdi.bv;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.glassfish.jersey.server.spi.ValidationInterceptor;
import org.glassfish.jersey.server.spi.ValidationInterceptorContext;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

/**
 * CDI backed interceptor to handle validation issues.
 *
 * @author Jaku Podlesak (jakub.podlesak at oracle.com)
 */
@ApplicationScoped
public class CdiValidationInterceptor implements ValidationInterceptor {

    private final CdiValidationResult validationResult;
    private final CdiPropertyInjectedResource pir;

    /**
     * Empty constructor to make CDI happy.
     */
    @SuppressWarnings("UnusedDeclaration")
    public CdiValidationInterceptor() {
        this.validationResult = null;
        this.pir = null;
    }

    /**
     * Injection constructor.
     *
     * @param validationResult  CDI implementation of validation result.
     * @param resource CDI property-injected JAX-RS resource.
     */
    @Inject
    public CdiValidationInterceptor(CdiValidationResult validationResult, CdiPropertyInjectedResource resource) {
        this.validationResult = validationResult;
        this.pir = resource;
    }

    @Override
    public void onValidate(ValidationInterceptorContext ctx) throws ValidationException {

        final Object resource = ctx.getResource();
        if (resource instanceof TargetInstanceProxy) {
            ctx.setResource(((TargetInstanceProxy) resource).getTargetInstance());
        }

        try {
            ctx.proceed();
        } catch (ConstraintViolationException constraintViolationException) {

            // First check for a property
            if (ValidationResultUtil.hasValidationResultProperty(resource)) {
                final Method validationResultGetter = ValidationResultUtil.getValidationResultGetter(resource);
                ValidationResultUtil.updateValidationResultProperty(resource, validationResultGetter,
                        constraintViolationException.getConstraintViolations());
                pir.setValidationResult(validationResult);
            } else {
                // Then check for a field
                final Field vr = ValidationResultUtil.getValidationResultField(resource);
                if (vr != null) {
                    // we have the right guy, no need to use reflection:
                    validationResult.setViolations(constraintViolationException.getConstraintViolations());
                } else {
                    if (isValidationResultInArgs(ctx.getArgs())) {
                        this.validationResult.setViolations(constraintViolationException.getConstraintViolations());
                    } else {
                        throw constraintViolationException;
                    }
                }
            }
        }
    }

    private boolean isValidationResultInArgs(Object[] args) {
        for (Object a : args) {
            if (a != null) {
                Class<?> argClass = a.getClass();
                do {
                    if (ValidationResult.class.isAssignableFrom(argClass)) {
                        return true;
                    }
                    argClass = argClass.getSuperclass();
                } while (argClass != Object.class);
            }
        }
        return false;
    }
}
