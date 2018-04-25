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

package org.glassfish.jersey.server.internal;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.InterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.JerseyPriorities;
import org.glassfish.jersey.message.MessageUtils;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.JSONP;

/**
 * A {@link WriterInterceptor} implementation for JSONP format. This interceptor wraps a JSON stream obtained by a underlying
 * JSON provider into a callback function that can be defined by the {@link JSONP} annotation.
 *
 * @author Michal Gajdos
 * @see JSONP
 */
@Priority(JerseyPriorities.POST_ENTITY_CODER)
// this interceptor has to run after content encoders (e.g. gzip/deflate), otherwise the added content (padding with the callback
// method call would not be encoded.
public class JsonWithPaddingInterceptor implements WriterInterceptor {

    private static final Map<String, Set<String>> JAVASCRIPT_TYPES;

    static {
        JAVASCRIPT_TYPES = new HashMap<>(2);

        JAVASCRIPT_TYPES.put("application", Arrays.asList("x-javascript", "ecmascript", "javascript")
                                                  .stream().collect(Collectors.toSet()));
        JAVASCRIPT_TYPES.put("text", Arrays.asList("javascript", "x-javascript", "ecmascript", "jscript")
                                           .stream().collect(Collectors.toSet()));
    }

    @Inject
    private Provider<ContainerRequest> containerRequestProvider;

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context) throws IOException, WebApplicationException {
        final boolean isJavascript = isJavascript(context.getMediaType());
        final JSONP jsonp = getJsonpAnnotation(context);

        final boolean wrapIntoCallback = isJavascript && jsonp != null;

        if (wrapIntoCallback) {
            context.setMediaType(MediaType.APPLICATION_JSON_TYPE);

            context.getOutputStream().write(getCallbackName(jsonp).getBytes(MessageUtils.getCharset(context.getMediaType())));
            context.getOutputStream().write('(');
        }

        context.proceed();

        if (wrapIntoCallback) {
            context.getOutputStream().write(')');
        }
    }

    /**
     * Returns a flag whether the given {@link MediaType media type} belongs to the group of JavaScript media types.
     *
     * @param mediaType media type to check.
     * @return {@code true} if the given media type is a JavaScript type, {@code false} otherwise (or if the media type is
     *         {@code null}}
     */
    private boolean isJavascript(final MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }

        final Set<String> subtypes = JAVASCRIPT_TYPES.get(mediaType.getType());
        return subtypes != null && subtypes.contains(mediaType.getSubtype());
    }

    /**
     * Returns a JavaScript callback name to wrap the JSON entity into. The callback name is determined from the {@link JSONP}
     * annotation.
     *
     * @param jsonp {@link JSONP} annotation to determine the callback name from.
     * @return a JavaScript callback name.
     */
    private String getCallbackName(final JSONP jsonp) {
        String callback = jsonp.callback();

        if (!"".equals(jsonp.queryParam())) {
            final ContainerRequest containerRequest = containerRequestProvider.get();
            final UriInfo uriInfo = containerRequest.getUriInfo();
            final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            final List<String> queryParameter = queryParameters.get(jsonp.queryParam());

            callback = (queryParameter != null && !queryParameter.isEmpty()) ? queryParameter.get(0) : callback;
        }

        return callback;
    }

    /**
     * Returns a {@link JSONP} annotation of the resource method responsible for handling the current request.
     *
     * @param context an {@link InterceptorContext interceptor context} to obtain the annotation from.
     * @return {@link JSONP} annotation or {@code null} if the resource method is not annotated with this annotation.
     * @see javax.ws.rs.ext.InterceptorContext#getAnnotations()
     */
    private JSONP getJsonpAnnotation(final InterceptorContext context) {
        final Annotation[] annotations = context.getAnnotations();

        if (annotations != null && annotations.length > 0) {
            for (final Annotation annotation : annotations) {
                if (annotation instanceof JSONP) {
                    return (JSONP) annotation;
                }
            }
        }

        return null;
    }
}
