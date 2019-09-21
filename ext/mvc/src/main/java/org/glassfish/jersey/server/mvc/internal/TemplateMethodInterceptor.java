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

import java.io.IOException;

import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.annotation.Priority;

import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

/**
 * Intercepts resource methods that are annotated by {@link Template template annotation} and does not return {@link Viewable}
 * instances.
 *
 * @see org.glassfish.jersey.server.mvc.spi.ResolvedViewable
 * @see ViewableMessageBodyWriter
 *
 * @author Michal Gajdos
 */
@Priority(Priorities.ENTITY_CODER)
class TemplateMethodInterceptor implements WriterInterceptor {

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
        final Template template = TemplateHelper.getTemplateAnnotation(context.getAnnotations());
        final Object entity = context.getEntity();

        if (template != null && !(entity instanceof Viewable)) {
            context.setType(Viewable.class);
            context.setEntity(new Viewable(template.name(), entity));
        }

        context.proceed();
    }
}
