/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 Payara Foundation  and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.server.validation.internal.hibernate.HibernateInjectingConstraintValidatorFactory;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

/**
 * {@link ConstraintValidatorFactory} implementation that uses {@link InjectingConstraintValidatorFactory}
 * by default and fallbacks to {@link HibernateInjectingConstraintValidatorFactory} when the resource
 * cannot be found in resource context of Jersey.
 *
 * @author Mert Caliskan
 */
public class CompositeInjectingConstraintValidatorFactory implements ConstraintValidatorFactory {

    @Context
    private ResourceContext resourceContext;

    private InjectingConstraintValidatorFactory jerseyVF;
    private HibernateInjectingConstraintValidatorFactory hibernateVF;

    @PostConstruct
    void postConstruct() {
        jerseyVF = resourceContext.getResource(InjectingConstraintValidatorFactory.class);
        hibernateVF = resourceContext.getResource(HibernateInjectingConstraintValidatorFactory.class);
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
        T jerseyInstance = jerseyVF.getInstance(key);
        if (jerseyInstance == null) {
            return hibernateVF.getInstance(key);
        }
        return jerseyInstance;
    }

    @Override
    public void releaseInstance(final ConstraintValidator<?, ?> instance) {
        // NOOP
    }
}
