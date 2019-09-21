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

package org.glassfish.jersey.server.mvc.internal;

import java.util.List;

import org.glassfish.jersey.server.mvc.Viewable;

/**
 * {@link Viewable} implementation representing return value of enhancing methods added to
 * {@link org.glassfish.jersey.server.model.Resource resources} annotated with {@link org.glassfish.jersey.server.mvc.Template}.
 *
 * @author Michal Gajdos
 * @see TemplateModelProcessor
 * @see org.glassfish.jersey.server.mvc.Template
 */
final class ImplicitViewable extends Viewable {

    private final List<String> templateNames;

    private final Class<?> resolvingClass;

    /**
     * Create a {@code ImplicitViewable}.
     *
     * @param templateNames allowed template names for which a {@link Viewable viewable} can be resolved.
     * @param model the model, may be {@code null}.
     * @param resolvingClass the class to use to resolve the template name if the template is not absolute,
     * if {@code null} then the resolving class will be obtained from the last matching resource.
     * @throws IllegalArgumentException if the template name is {@code null}.
     */
    ImplicitViewable(final List<String> templateNames, final Object model, final Class<?> resolvingClass)
            throws IllegalArgumentException {
        super("", model);

        this.templateNames = templateNames;
        this.resolvingClass = resolvingClass;
    }

    /**
     * Get allowed template names for which a {@link Viewable viewable} can be resolved.
     *
     * @return allowed template names.
     */
    public List<String> getTemplateNames() {
        return templateNames;
    }

    /**
     * Get the resolving class.
     *
     * @return Resolving class.
     */
    public Class<?> getResolvingClass() {
        return resolvingClass;
    }
}
