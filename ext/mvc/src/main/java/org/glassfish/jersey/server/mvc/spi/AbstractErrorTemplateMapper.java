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

package org.glassfish.jersey.server.mvc.spi;

import javax.ws.rs.core.Response;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.mvc.ErrorTemplate;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.internal.TemplateInflector;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

/**
 * Default implementation of {@link ExtendedExceptionMapper} used to declare special handling for exception types that should be
 * processed by MVC.
 * <p/>
 * Extensions should override {@link #getErrorStatus(Throwable)} and {@link #getErrorModel(Throwable)} to provide a response
 * status and model derived from a raised throwable.
 * <p/>
 * By default every {@link Exception exception} is mapped and used as a model in a viewable and passed to the MVC runtime for
 * further processing.
 *
 * @param <T> A type of the exception processed by the exception mapper.
 * @author Michal Gajdos
 * @since 2.3
 */
@Singleton
public abstract class AbstractErrorTemplateMapper<T extends Throwable> implements ExtendedExceptionMapper<T> {

    @Inject
    private javax.inject.Provider<ExtendedUriInfo> uriInfoProvider;

    @Override
    public final boolean isMappable(final T throwable) {
        return getErrorTemplate() != null;
    }

    /**
     * Get an {@link ErrorTemplate} annotation from resource method / class the throwable was raised from.
     *
     * @return an error template annotation or {@code null} if the method is not annotated.
     */
    private ErrorTemplate getErrorTemplate() {
        final ExtendedUriInfo uriInfo = uriInfoProvider.get();
        final ResourceMethod matchedResourceMethod = uriInfo.getMatchedResourceMethod();

        if (matchedResourceMethod != null) {
            final Invocable invocable = matchedResourceMethod.getInvocable();

            ErrorTemplate errorTemplate = invocable.getHandlingMethod().getAnnotation(ErrorTemplate.class);
            if (errorTemplate == null) {
                Class<?> handlerClass = invocable.getHandler().getHandlerClass();

                if (invocable.isInflector() && TemplateInflector.class
                        .isAssignableFrom(invocable.getHandler().getHandlerClass())) {

                    handlerClass = ((TemplateInflector) invocable.getHandler().getInstance(null)).getModelClass();
                }

                errorTemplate = handlerClass.getAnnotation(ErrorTemplate.class);
            }

            return errorTemplate;
        }

        return null;
    }

    @Override
    public final Response toResponse(final T throwable) {
        final ErrorTemplate error = getErrorTemplate();
        final String templateName = "".equals(error.name()) ? "index" : error.name();

        return Response
                .status(getErrorStatus(throwable))
                .entity(new Viewable(templateName, getErrorModel(throwable)))
                .build();
    }

    /**
     * Get a model for error template. Default value is the {@code throwable} itself.
     *
     * @param throwable throwable raised during processing a resource method.
     * @return a model for error template.
     */
    protected Object getErrorModel(final T throwable) {
        return throwable;
    }

    /**
     * Get a response status of to-be-processed error template. Default value is {@link Response.Status#OK}.
     *
     * @param throwable throwable raised during processing a resource method.
     * @return response status of error response.
     */
    protected Response.Status getErrorStatus(final T throwable) {
        return Response.Status.OK;
    }
}
