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

package org.glassfish.jersey.tests.e2e.server.mvc.provider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.internal.TemplateHelper;
import org.glassfish.jersey.server.mvc.spi.ResolvedViewable;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;
import org.glassfish.jersey.server.mvc.spi.ViewableContext;

/**
 * Custom {@link ViewableContext viewable context}.
 *
 * @author Michal Gajdos
 */
@Provider
public class CustomViewableContext implements ViewableContext {

    @Override
    public ResolvedViewable resolveViewable(final Viewable viewable, final MediaType mediaType,
                                            final Class<?> resourceClass, final TemplateProcessor templateProcessor) {
        final String path = TemplateHelper.getTemplateName(viewable);
        final Object templateReference = templateProcessor.resolve("/CustomViewableContext/" + path,
                MediaType.TEXT_PLAIN_TYPE);

        if (templateReference != null) {
            return new ResolvedViewable(templateProcessor, templateReference, viewable, MediaType.TEXT_PLAIN_TYPE);
        }

        return null;
    }
}
