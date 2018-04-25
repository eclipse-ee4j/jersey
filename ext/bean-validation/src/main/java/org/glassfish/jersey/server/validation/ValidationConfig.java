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

package org.glassfish.jersey.server.validation;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;

/**
 * Configuration class for Bean Validation provider.
 *
 * @author Michal Gajdos
 */
public final class ValidationConfig {

    private MessageInterpolator messageInterpolator;
    private TraversableResolver traversableResolver;
    private ConstraintValidatorFactory constraintValidatorFactory;
    private ParameterNameProvider parameterNameProvider;

    /**
     * Return {@code MessageInterpolator} implementation used for configuration.
     *
     * @return instance of {@code MessageInterpolator} or {@code null} if not defined.
     */
    public MessageInterpolator getMessageInterpolator() {
        return messageInterpolator;
    }

    /**
     * Return {@code TraversableResolver} implementation used for configuration.
     *
     * @return instance of {@code TraversableResolver} or {@code null} if not defined.
     */
    public TraversableResolver getTraversableResolver() {
        return traversableResolver;
    }

    /**
     * Return {@code ConstraintValidatorFactory} implementation used for configuration.
     *
     * @return instance of {@code ConstraintValidatorFactory} or {@code null} if not defined.
     */
    public ConstraintValidatorFactory getConstraintValidatorFactory() {
        return constraintValidatorFactory;
    }

    /**
     * Return {@code ParameterNameProvider} implementation used for configuration.
     *
     * @return instance of {@code ParameterNameProvider} or {@code null} if not defined.
     */
    public ParameterNameProvider getParameterNameProvider() {
        return parameterNameProvider;
    }

    /**
     * Defines the message interpolator.
     * If {@code null} is passed, the default message interpolator is used.
     *
     * @param messageInterpolator message interpolator implementation.
     */
    public ValidationConfig messageInterpolator(final MessageInterpolator messageInterpolator) {
        this.messageInterpolator = messageInterpolator;
        return this;
    }

    /**
     * Defines the traversable resolver.
     * If {@code null} is passed, the default traversable resolver is used.
     *
     * @param traversableResolver traversable resolver implementation.
     */
    public ValidationConfig traversableResolver(final TraversableResolver traversableResolver) {
        this.traversableResolver = traversableResolver;
        return this;
    }

    /**
     * Defines the constraint validator factory.
     * If {@code null} is passed, the default constraint validator factory is used.
     *
     * @param constraintValidatorFactory constraint factory implementation.
     */
    public ValidationConfig constraintValidatorFactory(final ConstraintValidatorFactory constraintValidatorFactory) {
        this.constraintValidatorFactory = constraintValidatorFactory;
        return this;
    }

    /**
     * Defines the parameter name provider.
     * If {@code null} is passed, the default parameter name provider is used.
     *
     * @param parameterNameProvider parameter name provider implementation.
     */
    public ValidationConfig parameterNameProvider(final ParameterNameProvider parameterNameProvider) {
        this.parameterNameProvider = parameterNameProvider;
        return this;
    }
}
