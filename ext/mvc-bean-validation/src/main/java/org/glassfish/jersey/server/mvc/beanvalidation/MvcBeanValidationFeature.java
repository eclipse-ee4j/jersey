/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.mvc.beanvalidation;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.validation.ValidationFeature;

/**
 * {@link Feature} used to add support for {@link MvcFeature MVC} and Bean Validation.
 * <p/>
 * {@link org.glassfish.jersey.server.mvc.Viewable Viewable} (template) defined by
 * {@link org.glassfish.jersey.server.mvc.ErrorTemplate ErrorTemplate} annotation, present directly on an executed resource method
 * or on a resource class the resource method is defined in, is processed to display an error message caused by an
 * {@link javax.validation.ConstraintViolationException Bean Validation exception}. Model is, in this case, a list of
 * {@link org.glassfish.jersey.server.validation.ValidationError validation errors}.
 * <p/>
 * Note: This feature also registers {@link MvcFeature}.
 *
 * @author Michal Gajdos
 * @see org.glassfish.jersey.server.mvc.ErrorTemplate
 * @since 2.3
 */
@ConstrainedTo(RuntimeType.SERVER)
public class MvcBeanValidationFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (!config.isRegistered(ValidationErrorTemplateExceptionMapper.class)) {
            // Exception Mapper.
            context.register(ValidationErrorTemplateExceptionMapper.class);

            // BeanValidation feature.
            if (!config.isRegistered(ValidationFeature.class)) {
                context.register(ValidationFeature.class);
            }

            // Mvc feature.
            if (!config.isRegistered(MvcFeature.class)) {
                context.register(MvcFeature.class);
            }

            return true;
        }
        return false;
    }
}
