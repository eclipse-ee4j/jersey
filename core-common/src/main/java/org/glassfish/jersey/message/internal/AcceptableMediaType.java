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

package org.glassfish.jersey.message.internal;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Map;

import javax.ws.rs.core.MediaType;

/**
 * An acceptable media type.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class AcceptableMediaType extends MediaType implements Qualified {
    /**
     * Comparator for collections of acceptable media types.
     */
    public static final Comparator<AcceptableMediaType> COMPARATOR = new Comparator<AcceptableMediaType>() {

        @Override
        public int compare(AcceptableMediaType o1, AcceptableMediaType o2) {
            int i = Quality.QUALIFIED_COMPARATOR.compare(o1, o2);
            if (i != 0) {
                return i;
            }

            return MediaTypes.PARTIAL_ORDER_COMPARATOR.compare(o1, o2);
        }
    };

    private final int q;

    /**
     * Create new acceptable media type instance with a {@link Quality#DEFAULT
     * default quality factor} value.
     *
     * @param type    the primary type, {@code null} is equivalent to
     *                {@link #MEDIA_TYPE_WILDCARD}
     * @param subtype the subtype, null is equivalent to
     *                {@link #MEDIA_TYPE_WILDCARD}
     */
    public AcceptableMediaType(String type, String subtype) {
        super(type, subtype); // no need to add default quality parameter.
        q = Quality.DEFAULT;
    }

    /**
     * Create new acceptable media type instance.
     *
     * @param type       the primary type, {@code null} is equivalent to
     *                   {@link #MEDIA_TYPE_WILDCARD}
     * @param subtype    the subtype, {@code null} is equivalent to
     *                   {@link #MEDIA_TYPE_WILDCARD}
     * @param quality    quality factor value in [ppt]. See {@link Qualified}.
     * @param parameters a map of media type parameters, {@code null} is the same as an
     *                   empty map.
     */
    public AcceptableMediaType(String type, String subtype, int quality, Map<String, String> parameters) {
        super(type, subtype, Quality.enhanceWithQualityParameter(parameters, Quality.QUALITY_PARAMETER_NAME, quality));
        this.q = quality;
    }


    // used by AcceptableMediaType.valueOf methods; no need to fix parameter map
    private AcceptableMediaType(String type, String subtype, Map<String, String> parameters, int quality) {
        super(type, subtype, parameters);
        this.q = quality;
    }

    @Override
    public int getQuality() {
        return q;
    }

    /**
     * Create new acceptable media type instance from the supplied
     * {@link HttpHeaderReader HTTP header reader}.
     *
     * @param reader HTTP header reader.
     * @return new acceptable media type instance.
     *
     * @throws ParseException in case the input data parsing failed.
     */
    public static AcceptableMediaType valueOf(HttpHeaderReader reader) throws ParseException {
        // Skip any white space
        reader.hasNext();

        // Get the type
        String type = reader.nextToken().toString();
        String subType = "*";
        // Some HTTP implements use "*" to mean "*/*"
        if (reader.hasNextSeparator('/', false)) {
            reader.next(false);
            // Get the subtype
            subType = reader.nextToken().toString();
        }

        Map<String, String> parameters = null;
        int quality = Quality.DEFAULT;
        if (reader.hasNext()) {
            parameters = HttpHeaderReader.readParameters(reader);
            if (parameters != null) {
                String v = parameters.get(Quality.QUALITY_PARAMETER_NAME);
                if (v != null) {
                    quality = HttpHeaderReader.readQualityFactor(v);
                }
            }
        }

        // use private constructor to skip quality value validation step
        return new AcceptableMediaType(type, subType, parameters, quality);
    }

    /**
     * Create new acceptable media type instance from the supplied
     * {@link javax.ws.rs.core.MediaType media type}.
     *
     * @param mediaType general-purpose media type.
     * @return new acceptable media type instance.
     *
     * @throws ParseException in case the quality parameter parsing failed.
     */
    public static AcceptableMediaType valueOf(MediaType mediaType) throws ParseException {
        if (mediaType instanceof AcceptableMediaType) {
            return (AcceptableMediaType) mediaType;
        }

        final Map<String, String> parameters = mediaType.getParameters();

        int quality = Quality.DEFAULT;
        if (parameters != null) {
            final String v = parameters.get(Quality.QUALITY_PARAMETER_NAME);
            if (v != null) {
                quality = HttpHeaderReader.readQualityFactor(v);
            }
        }

        // use private constructor to skip quality value validation step
        return new AcceptableMediaType(mediaType.getType(), mediaType.getSubtype(), parameters, quality);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        if (obj instanceof AcceptableMediaType) {
            final AcceptableMediaType other = (AcceptableMediaType) obj;
            return this.q == other.q;
        } else {
            // obj is a plain MediaType instance
            // with a quality factor set to default (1.0)
            return this.q == Quality.DEFAULT;
        }
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        return (this.q == Quality.DEFAULT) ? hash : 47 * hash + this.q;
    }
}
