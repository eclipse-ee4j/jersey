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

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.ResolvedViewable;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;
import org.glassfish.jersey.server.mvc.spi.ViewableContext;
import org.glassfish.jersey.server.mvc.spi.ViewableContextException;

/**
 * Default implementation of {@link org.glassfish.jersey.server.mvc.spi.ViewableContext viewable context}.
 * <p/>
 * If the template name of given {@link Viewable} is represented as a relative path then the resolving class,
 * and super classes in the inheritance hierarchy, are utilized to generate the absolute template name as follows.
 * <br/>
 * The base path starts with '/' character, followed by the fully qualified class name of the resolving class,
 * with any '.' and '$' characters replaced with a '/' character, followed by a '/' character,
 * followed by the relative template name.
 * <br/>
 * If the absolute template name cannot be resolved into a template reference (see {@link org.glassfish.jersey.server.mvc.spi
 * .TemplateProcessor} and {@link org.glassfish.jersey.server.mvc.spi.ViewableContext}) then the super class of the resolving
 * class is utilized, and is set as the resolving class. Traversal up the inheritance hierarchy proceeds until an absolute
 * template name can be resolved into a template reference, or the Object class is reached,
 * which means the absolute template name could not be resolved and an error will result.
 *
 * @author Michal Gajdos
 */
class ResolvingViewableContext implements ViewableContext {

    /**
     * Resolve given {@link Viewable viewable} using {@link MediaType media type}, {@code resolving class} and
     * {@link TemplateProcessor template processor}.
     *
     * @param viewable viewable to be resolved.
     * @param mediaType media type of te output.
     * @param resourceClass resolving class.
     * @param templateProcessor template processor to be used.
     * @return resolved viewable or {@code null} if the viewable cannot be resolved.
     */
    public ResolvedViewable resolveViewable(final Viewable viewable, final MediaType mediaType,
                                            final Class<?> resourceClass, final TemplateProcessor templateProcessor) {
        if (viewable.isTemplateNameAbsolute()) {
            return resolveAbsoluteViewable(viewable, resourceClass, mediaType, templateProcessor);
        } else {
            if (resourceClass == null) {
                throw new ViewableContextException(LocalizationMessages.TEMPLATE_RESOLVING_CLASS_CANNOT_BE_NULL());
            }

            return resolveRelativeViewable(viewable, resourceClass, mediaType, templateProcessor);
        }
    }

    /**
     * Resolve given {@link Viewable viewable} with absolute template name using {@link MediaType media type} and
     * {@link TemplateProcessor template processor}.
     *
     * @param viewable viewable to be resolved.
     * @param mediaType media type of te output.
     * @param resourceClass resource class.
     * @param templateProcessor template processor to be used.
     * @return resolved viewable or {@code null} if the viewable cannot be resolved.
     */
    @SuppressWarnings("unchecked")
    private ResolvedViewable resolveAbsoluteViewable(final Viewable viewable, Class<?> resourceClass,
                                                     final MediaType mediaType,
                                                     final TemplateProcessor templateProcessor) {
        final Object resolvedTemplateObject = templateProcessor.resolve(viewable.getTemplateName(), mediaType);

        if (resolvedTemplateObject != null) {
            return new ResolvedViewable(templateProcessor, resolvedTemplateObject, viewable, resourceClass, mediaType);
        }

        return null;
    }

    /**
     * Resolve given {@link Viewable viewable} with relative template name using {@link MediaType media type},
     * {@code resolving class} and {@link TemplateProcessor template processor}.
     *
     * @param viewable viewable to be resolved.
     * @param mediaType media type of te output.
     * @param resolvingClass resolving class.
     * @param templateProcessor template processor to be used.
     * @return resolved viewable or {@code null} if the viewable cannot be resolved.
     */
    @SuppressWarnings("unchecked")
    private ResolvedViewable resolveRelativeViewable(final Viewable viewable, final Class<?> resolvingClass,
                                                     final MediaType mediaType, final TemplateProcessor templateProcessor) {
        final String path = TemplateHelper.getTemplateName(viewable);

        // Find in directories.
        for (Class c = resolvingClass; c != Object.class; c = c.getSuperclass()) {
            final String absolutePath = TemplateHelper.getAbsolutePath(c, path, '/');
            final Object resolvedTemplateObject = templateProcessor.resolve(absolutePath, mediaType);

            if (resolvedTemplateObject != null) {
                return new ResolvedViewable(templateProcessor, resolvedTemplateObject, viewable, c, mediaType);
            }
        }

        // Find in flat files.
        for (Class c = resolvingClass; c != Object.class; c = c.getSuperclass()) {
            final String absolutePath = TemplateHelper.getAbsolutePath(c, path, '.');
            final Object resolvedTemplateObject = templateProcessor.resolve(absolutePath, mediaType);

            if (resolvedTemplateObject != null) {
                return new ResolvedViewable(templateProcessor, resolvedTemplateObject, viewable, c, mediaType);
            }
        }

        return null;
    }

}
