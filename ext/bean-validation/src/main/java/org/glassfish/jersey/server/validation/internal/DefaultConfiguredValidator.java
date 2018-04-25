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

package org.glassfish.jersey.server.validation.internal;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.MethodDescriptor;

import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.ValidationInterceptor;
import org.glassfish.jersey.server.spi.ValidationInterceptorContext;

/**
 * Default {@link ConfiguredValidator} implementation - delegates calls to the underlying {@link Validator}.
 *
 * @author Michal Gajdos
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
class DefaultConfiguredValidator implements ConfiguredValidator, ValidationInterceptor {

    private final Validator delegate;
    private final Configuration configuration;
    private final ValidateOnExecutionHandler validateOnExecutionHandler;
    private final List<ValidationInterceptor> interceptors;

    /**
     * Create a configured validator instance.
     *
     * @param delegate                   validator to delegate calls to.
     * @param configuration              configuration to obtain {@link ExecutableType executable types} configured in descriptor
     *                                   from.
     * @param validateOnExecutionHandler handler for processing {@link javax.validation.executable.ValidateOnExecution}
     *                                   annotations.
     * @param interceptors               custom validation interceptors.
     */
    DefaultConfiguredValidator(final Validator delegate, final Configuration configuration,
                               final ValidateOnExecutionHandler validateOnExecutionHandler,
                               final Iterable<ValidationInterceptor> interceptors) {
        this.delegate = delegate;
        this.configuration = configuration;
        this.validateOnExecutionHandler = validateOnExecutionHandler;
        this.interceptors = createInterceptorList(interceptors);
    }

    private List<ValidationInterceptor> createInterceptorList(Iterable<ValidationInterceptor> interceptors) {
        List<ValidationInterceptor> result = new LinkedList<>();
        for (ValidationInterceptor i : interceptors) {
            result.add(i);
        }
        result.add(this);
        return result;
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validate(final T object, final Class<?>... groups) {
        return delegate.validate(object, groups);
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(final T object, final String propertyName, final Class<?>... groups) {
        return delegate.validateProperty(object, propertyName, groups);
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(final Class<T> beanType, final String propertyName,
                                                         final Object value, final Class<?>... groups) {
        return delegate.validateValue(beanType, propertyName, value, groups);
    }

    @Override
    public BeanDescriptor getConstraintsForClass(final Class<?> clazz) {
        return delegate.getConstraintsForClass(clazz);
    }

    @Override
    public <T> T unwrap(final Class<T> type) {
        return delegate.unwrap(type);
    }

    @Override
    public ExecutableValidator forExecutables() {
        return delegate.forExecutables();
    }

    @Override
    public void validateResourceAndInputParams(final Object resource, final Invocable resourceMethod, final Object[] args) {

        ValidationInterceptorExecutor validationExecutor = new ValidationInterceptorExecutor(
                resource,
                resourceMethod,
                args,
                interceptors.iterator());

        validationExecutor.proceed();
    }

    // Invoked as the last validation interceptor method in the chain.
    @Override
    public void onValidate(final ValidationInterceptorContext ctx) {

        final Object resource = ctx.getResource();
        final Invocable resourceMethod = ctx.getInvocable();
        final Object[] args = ctx.getArgs();

        final Set<ConstraintViolation<Object>> constraintViolations = new HashSet<>();
        final BeanDescriptor beanDescriptor = getConstraintsForClass(resource.getClass());

        // Resource validation.
        if (beanDescriptor.isBeanConstrained()) {
            constraintViolations.addAll(validate(resource));
        }

        if (resourceMethod != null
                && configuration.getBootstrapConfiguration().isExecutableValidationEnabled()) {
            final Method handlingMethod = resourceMethod.getHandlingMethod();

            // Resource method validation - input parameters.
            final MethodDescriptor methodDescriptor = beanDescriptor.getConstraintsForMethod(handlingMethod.getName(),
                    handlingMethod.getParameterTypes());

            if (methodDescriptor != null
                    && methodDescriptor.hasConstrainedParameters()
                    && validateOnExecutionHandler.validateMethod(resource.getClass(),
                                                                 resourceMethod.getDefinitionMethod(),
                                                                 resourceMethod.getHandlingMethod())) {
                constraintViolations.addAll(forExecutables().validateParameters(resource, handlingMethod, args));
            }
        }

        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public void validateResult(final Object resource, final Invocable resourceMethod, final Object result) {
        if (configuration.getBootstrapConfiguration().isExecutableValidationEnabled()) {
            final Set<ConstraintViolation<Object>> constraintViolations = new HashSet<>();
            final Method handlingMethod = resourceMethod.getHandlingMethod();

            final BeanDescriptor beanDescriptor = getConstraintsForClass(resource.getClass());
            final MethodDescriptor methodDescriptor = beanDescriptor.getConstraintsForMethod(handlingMethod.getName(),
                    handlingMethod.getParameterTypes());

            final Method definitionMethod = resourceMethod.getDefinitionMethod();

            if (methodDescriptor != null
                    && methodDescriptor.hasConstrainedReturnValue()
                    && validateOnExecutionHandler.validateMethod(resource.getClass(), definitionMethod, handlingMethod)) {
                constraintViolations.addAll(forExecutables().validateReturnValue(resource, handlingMethod, result));

                if (result instanceof Response) {
                    constraintViolations.addAll(forExecutables().validateReturnValue(resource, handlingMethod,
                            ((Response) result).getEntity()));
                }
            }

            if (!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
        }
    }
}
