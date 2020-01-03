/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.routing;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.WriterModel;
import org.glassfish.jersey.message.internal.AcceptableMediaType;
import org.glassfish.jersey.message.internal.MediaTypes;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Utility class containing methods used on both client and server side for determining media type of a response based
 * on provided {@link MessageBodyWorkers}.
 */
public class ContentTypeDeterminer {

    protected ContentTypeDeterminer(MessageBodyWorkers workers) {
        this.workers = workers;
    }

    protected MessageBodyWorkers workers;

    /**
     * Determine the {@link MediaType} of the {@link Response} based on writers suitable for the given entity class,
     * pre-selected method and acceptable media types.
     *
     * @param entityClass          entity class to determine the media type for.
     * @param entityType           entity type for writers.
     * @param selectedMethod       pre-selected (invoked) method.
     * @param acceptableMediaTypes acceptable media types from request.
     * @param methodProducesTypes  media types producible by resource method
     * @return                     media type of the response.
     */
    protected MediaType determineResponseMediaType(
            final Class<?> entityClass,
            final Type entityType,
            final RequestSpecificConsumesProducesAcceptor<?> selectedMethod,
            final List<AcceptableMediaType> acceptableMediaTypes,
            final List<MediaType> methodProducesTypes,
            final Annotation[] handlingMethodAnnotations) {

        // Entity class can be null when considering HEAD method || empty entity.
        final Class<?> responseEntityClass = entityClass;

        // Applicable entity providers
        final List<WriterModel> writersForEntityType = workers.getWritersModelsForType(responseEntityClass);

        CombinedMediaType selected = null;
        for (final MediaType acceptableMediaType : acceptableMediaTypes) {
            for (final MediaType methodProducesType : methodProducesTypes) {
                if (!acceptableMediaType.isCompatible(methodProducesType)) {
                    // no need to go deeper if acceptable and method produces type are incompatible
                    continue;
                }

                // Use writers suitable for entity class to determine the media type.
                for (final WriterModel model : writersForEntityType) {
                    for (final MediaType writerProduces : model.declaredTypes()) {
                        if (!writerProduces.isCompatible(acceptableMediaType)
                                || !methodProducesType.isCompatible(writerProduces)) {
                            continue;
                        }

                        final CombinedMediaType.EffectiveMediaType effectiveProduces =
                                new CombinedMediaType.EffectiveMediaType(
                                        MediaTypes.mostSpecific(methodProducesType, writerProduces),
                                        false);

                        final CombinedMediaType candidate =
                                CombinedMediaType.create(acceptableMediaType, effectiveProduces);

                        if (candidate != CombinedMediaType.NO_MATCH) {
                            // Look for a better compatible worker.
                            if (selected == null || CombinedMediaType.COMPARATOR.compare(candidate, selected) < 0) {
                                if (model.isWriteable(
                                        responseEntityClass,
                                        entityType,
                                        handlingMethodAnnotations,
                                        candidate.getCombinedType())) {
                                    selected = candidate;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Found media type for current writer.
        if (selected != null) {
            return selected.getCombinedType();
        }

        // If the media type couldn't be determined, choose pre-selected one and wait whether interceptors change the mediaType
        // so it can be written.
        return selectedMethod.getProduces().getCombinedType();
    }
}
