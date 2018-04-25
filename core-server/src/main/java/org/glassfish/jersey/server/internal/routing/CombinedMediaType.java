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

package org.glassfish.jersey.server.internal.routing;

import java.util.Comparator;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.message.internal.Quality;
import org.glassfish.jersey.message.internal.QualitySourceMediaType;

/**
 * Represents function S as defined in the Request Matching part of the spec.
 *
 * @author Jakub Podlesak
 * @author Miroslav Fuksa
 */
final class CombinedMediaType {

    /**
     * Constant combined type representing no match.
     */
    static final CombinedMediaType NO_MATCH = new CombinedMediaType(null, 0, 0, 0);

    private static int matchedWildcards(MediaType clientMt, EffectiveMediaType serverMt) {
        return b2i(clientMt.isWildcardType() ^ serverMt.isWildcardType())
                + b2i(clientMt.isWildcardSubtype() ^ serverMt.isWildcardSubType());
    }

    private static int b2i(boolean b) {
        return b ? 1 : 0;
    }

    /**
     * Combined client/server media type, stripped of q and qs parameters.
     */
    final MediaType combinedType;
    /**
     * Client-specified media type quality.
     */
    final int q;
    /**
     * Server-specified media type quality.
     */
    final int qs;
    /**
     * Distance of the combined media types.
     * <ul>
     * <li>
     * 0 - if the type and subtype of both combined media types match exactly (i.e. ["m/n" + "m/n"]).
     * </li>
     * <li>
     * 1 - if one media type contains a wildcard type or subtype value that matches a concrete type or subtype value.
     * </li>
     * <li>
     * 2 - if one of the media types is a {@link MediaType#WILDCARD_TYPE} and the other one is a concrete media type.
     * </li>
     * </ul>
     */
    final int d;

    private CombinedMediaType(final MediaType combinedType, final int q, final int qs, final int d) {
        this.combinedType = combinedType;
        this.q = q;
        this.qs = qs;
        this.d = d;
    }

    /**
     * Create combined client/server media type.
     *
     * if the two types are not compatible, {@link #NO_MATCH} is returned.
     *
     * @param clientType client-side media type.
     * @param serverType server-side media type.
     * @return combined client/server media type.
     */
    static CombinedMediaType create(MediaType clientType, EffectiveMediaType serverType) {
        if (!clientType.isCompatible(serverType.getMediaType())) {
            return NO_MATCH;
        }

        final MediaType strippedClientType = MediaTypes.stripQualityParams(clientType);
        final MediaType strippedServerType = MediaTypes.stripQualityParams(serverType.getMediaType());

        return new CombinedMediaType(
                MediaTypes.mostSpecific(strippedClientType, strippedServerType),
                MediaTypes.getQuality(clientType),
                QualitySourceMediaType.getQualitySource(serverType.getMediaType()),
                matchedWildcards(clientType, serverType));
    }

    /**
     * Comparator used to compare {@link CombinedMediaType}. The comparator sorts the elements of list
     * in the ascending order from the most appropriate to the least appropriate combined media type.
     */
    static final Comparator<CombinedMediaType> COMPARATOR = new Comparator<CombinedMediaType>() {

        @Override
        public int compare(CombinedMediaType c1, CombinedMediaType c2) {
            // more concrete is better
            int delta = MediaTypes.PARTIAL_ORDER_COMPARATOR.compare(c1.combinedType, c2.combinedType);
            if (delta != 0) {
                return delta;
            }

            // higher is better
            delta = Quality.QUALITY_VALUE_COMPARATOR.compare(c1.q, c2.q);
            if (delta != 0) {
                return delta;
            }

            // higher is better
            delta = Quality.QUALITY_VALUE_COMPARATOR.compare(c1.qs, c2.qs);
            if (delta != 0) {
                return delta;
            }

            // lower is better
            return Integer.compare(c1.d, c2.d);
        }
    };

    @Override
    public String toString() {
        return String.format("%s;q=%d;qs=%d;d=%d", combinedType, q, qs, d);
    }

    /**
     * {@link MediaType Media type} extended by flag indicating whether media type was
     * obtained from user annotations {@link Consumes} or {@link Produces} or has no
     * annotation and therefore was derived from {@link MessageBodyWorkers}.
     */
    static class EffectiveMediaType {

        /**
         * True if the MediaType was not defined by annotation and therefore was
         * derived from Message Body Providers.
         */
        private final boolean derived;
        private final MediaType mediaType;

        /**
         * Creates new instance with {@code mediaType} and flag indicating the origin of
         * the mediaType.
         *
         * @param mediaType                The media type.
         * @param fromMessageBodyProviders True if {@code mediaType} was derived from
         *                                 {@link MessageBodyWorkers}.
         */
        public EffectiveMediaType(MediaType mediaType, boolean fromMessageBodyProviders) {
            this.derived = fromMessageBodyProviders;
            this.mediaType = mediaType;
        }

        /**
         * Creates new instance with {@code mediaType} which was obtained from user
         * annotations {@link Consumes} or {@link Produces}.
         *
         * @param mediaTypeValue The string media type.
         */
        public EffectiveMediaType(String mediaTypeValue) {
            this(MediaType.valueOf(mediaTypeValue), false);
        }

        /**
         * Creates new instance with {@code mediaType} which was obtained from user
         * annotations {@link Consumes} or {@link Produces}.
         *
         * @param mediaType The media type.
         */
        public EffectiveMediaType(MediaType mediaType) {
            this(mediaType, false);
        }

        /**
         * Returns true if Type of {@link MediaType} was originally  defined as wildcard.
         *
         * @return Returns true if method {@link Consumes} or {@link Produces} was
         * annotated with wildcard type (for example '*&#47;*').
         */
        public boolean isWildcardType() {
            return mediaType.isWildcardType();
        }

        /**
         * Returns True if SubType of {@link MediaType} was originally defined as wildcard.
         *
         * @return Returns true if method {@link Consumes} or {@link Produces} was
         * annotated with wildcard subtype (for example 'text&#47;*').
         */
        public boolean isWildcardSubType() {
            return mediaType.isWildcardSubtype();
        }

        /**
         * Returns {@link MediaType}.
         *
         * @return Media type.
         */
        public MediaType getMediaType() {
            return mediaType;
        }

        /**
         * Return flag value whether the {@code MediaType} was not defined by annotation and therefore was derived from
         * Message Body Providers.
         *
         * @return {@code true} if the {@code MediaType} was not defined by annotation and therefore was derived from
         * Message Body Providers, {@code false} otherwise.
         */
        boolean isDerived() {
            return derived;
        }

        @Override
        public String toString() {
            return String.format("mediaType=[%s], fromProviders=%b", mediaType, derived);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof EffectiveMediaType)) {
                return false;
            }

            EffectiveMediaType that = (EffectiveMediaType) o;

            return derived == that.derived && mediaType.equals(that.mediaType);
        }

        @Override
        public int hashCode() {
            int result = (derived ? 1 : 0);
            result = 31 * result + mediaType.hashCode();
            return result;
        }
    }

}
