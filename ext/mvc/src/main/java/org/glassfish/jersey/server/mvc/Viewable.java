/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.mvc;

import org.glassfish.jersey.server.mvc.internal.LocalizationMessages;

/**
 * A viewable type referencing a template by name and a model to be passed
 * to the template. Such a type may be returned by a resource method of a
 * resource class. In this respect the template is the view and the controller
 * is the resource class in the Model View Controller pattern.
 * <p/>
 * The template name may be declared as absolute template name if the name
 * begins with a '/', otherwise the template name is declared as a relative
 * template name. If the template name is relative then the class of the
 * last matching resource is utilized to create an absolute path by default. However,
 * the responsibility of resolving the absolute template name is up to
 * {@link org.glassfish.jersey.server.mvc.spi.ViewableContext} which can override the
 * default resolving behaviour.
 * <p/>
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 *
 * @see Template
 * @see org.glassfish.jersey.server.mvc.spi.ViewableContext
 * @see org.glassfish.jersey.server.mvc.internal.ResolvingViewableContext
 */
public class Viewable {

    private final String templateName;

    private final Object model;

    /**
     * Construct a new viewable type with a template name.
     * <p/>
     * The model will be set to {@code null}.
     *
     * @param templateName the template name, shall not be {@code null}.
     * @throws IllegalArgumentException if the template name is {@code null}.
     */
    public Viewable(String templateName) throws IllegalArgumentException {
        this(templateName, null);
    }

    /**
     * Construct a new viewable type with a template name and a model.
     *
     * @param templateName the template name, shall not be {@code null}.
     * @param model the model, may be {@code null}.
     * @throws IllegalArgumentException if the template name is {@code null}.
     */
    public Viewable(String templateName, Object model) throws IllegalArgumentException {
        if (templateName == null) {
            throw new IllegalArgumentException(LocalizationMessages.TEMPLATE_NAME_MUST_NOT_BE_NULL());
        }

        this.templateName = templateName;
        this.model = model;
    }

    /**
     * Get the template name.
     *
     * @return the template name.
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Get the model.
     *
     * @return the model.
     */
    public Object getModel() {
        return model;
    }


    /**
     * Determines whether the template name is represented by an absolute path.
     *
     * @return {@code true} if the template name is absolute, and starts with a
     *         '/' character, {@code false} otherwise.
     */
    public boolean isTemplateNameAbsolute() {
        return templateName.length() > 0 && templateName.charAt(0) == '/';
    }
}
