/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.internal.routing;

import org.glassfish.jersey.internal.routing.CombinedMediaType;
import org.glassfish.jersey.internal.routing.ContentTypeDeterminer;
import org.glassfish.jersey.internal.routing.RequestSpecificConsumesProducesAcceptor;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.AcceptableMediaType;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

/**
 * Client side {@link Response} media type determinator utility class.
 * Used for determining media type when {@link ClientRequestContext#abortWith(Response)} on
 * {@link javax.ws.rs.client.ClientRequestFilter#filter(ClientRequestContext)} is used without specifying the response
 * media type.
 */
public class AbortedRequestMediaTypeDeterminer extends ContentTypeDeterminer {

    private static final AbortedRouting ABORTED_ROUTING = new AbortedRouting();

    public AbortedRequestMediaTypeDeterminer(MessageBodyWorkers workers) {
        super(workers);
    }

    /**
     * Determine the {@link MediaType} of the {@link Response} based on writers suitable for the given entity instance.
     *
     * @param entity                entity instance to determine the media type for
     * @param acceptableMediaTypes  acceptable media types from request.
     * @return                      media type of the response.
     */
    public MediaType determineResponseMediaType(
            final Object entity,
            final List<AcceptableMediaType> acceptableMediaTypes) {

        final GenericType type = ReflectionHelper.genericTypeFor(entity);
        final CombinedMediaType wildcardType = CombinedMediaType.create(MediaType.WILDCARD_TYPE,
                new CombinedMediaType.EffectiveMediaType(MediaType.WILDCARD_TYPE));
        final RequestSpecificConsumesProducesAcceptor<AbortedRouting> selectedMethod =
                new RequestSpecificConsumesProducesAcceptor<>(wildcardType, wildcardType, true, ABORTED_ROUTING);
        // Media types producible by method.
        final List<MediaType> methodProducesTypes = Collections.singletonList(MediaType.WILDCARD_TYPE);

        return determineResponseMediaType(type.getRawType(), type.getType(), selectedMethod, acceptableMediaTypes,
                methodProducesTypes, null);
    }

    /**
     * An information that the routing aborted by  {@code javax.ws.rs.client.ClientRequestContext#abortWith(Response)}.
     */
    private static final class AbortedRouting {

        @Override
        public String toString() {
            return "{Aborted by ClientRequestFilter}";
        }
    }

}
