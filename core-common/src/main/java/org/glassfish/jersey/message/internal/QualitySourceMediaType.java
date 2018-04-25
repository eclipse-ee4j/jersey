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
 * A quality source media type.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class QualitySourceMediaType extends MediaType implements Qualified {
    /**
     * Comparator for lists of quality source media types.
     */
    public static final Comparator<QualitySourceMediaType> COMPARATOR =
            new Comparator<QualitySourceMediaType>() {

                @Override
                public int compare(QualitySourceMediaType o1, QualitySourceMediaType o2) {
                    int i = Quality.QUALIFIED_COMPARATOR.compare(o1, o2);
                    if (i != 0) {
                        return i;
                    }
                    return MediaTypes.PARTIAL_ORDER_COMPARATOR.compare(o1, o2);
                }
            };

    private final int qs;

    /**
     * Create new quality source media type instance with a {@link Quality#DEFAULT
     * default quality factor} value.
     *
     * @param type    the primary type, {@code null} is equivalent to
     *                {@link #MEDIA_TYPE_WILDCARD}
     * @param subtype the subtype, {@code null} is equivalent to
     *                {@link #MEDIA_TYPE_WILDCARD}
     */
    public QualitySourceMediaType(String type, String subtype) {
        super(type, subtype); // no need to add default quality parameter.
        qs = Quality.DEFAULT;
    }

    /**
     * Create new quality source media type instance.
     *
     * @param type       the primary type, {@code null} is equivalent to
     *                   {@link #MEDIA_TYPE_WILDCARD}
     * @param subtype    the subtype, {@code null} is equivalent to
     *                   {@link #MEDIA_TYPE_WILDCARD}
     * @param quality    quality source factor value in [ppt]. See {@link Qualified}.
     * @param parameters a map of media type parameters, {@code null} is the same as an
     *                   empty map.
     */
    public QualitySourceMediaType(String type, String subtype, int quality, Map<String, String> parameters) {
        super(type, subtype, Quality.enhanceWithQualityParameter(parameters, Quality.QUALITY_SOURCE_PARAMETER_NAME, quality));
        this.qs = quality;
    }

    // used by QualitySourceMediaType.valueOf method; no need to fix parameter map
    private QualitySourceMediaType(String type, String subtype, Map<String, String> parameters, int quality) {
        super(type, subtype, parameters);
        this.qs = quality;
    }

    /**
     * Get quality source factor value (in [ppt]).
     *
     * @return quality source factor value.
     */
    @Override
    public int getQuality() {
        return qs;
    }

    /**
     * Create new quality source media type instance from the supplied
     * {@link HttpHeaderReader HTTP header reader}.
     *
     * @param reader HTTP header reader.
     * @return new acceptable media type instance.
     *
     * @throws ParseException in case the input data parsing failed.
     */
    public static QualitySourceMediaType valueOf(HttpHeaderReader reader) throws ParseException {
        // Skip any white space
        reader.hasNext();

        // Get the type
        String type = reader.nextToken().toString();
        reader.nextSeparator('/');
        // Get the subtype
        String subType = reader.nextToken().toString();

        int qs = Quality.DEFAULT;
        Map<String, String> parameters = null;
        if (reader.hasNext()) {
            parameters = HttpHeaderReader.readParameters(reader);
            if (parameters != null) {
                qs = getQs(parameters.get(Quality.QUALITY_SOURCE_PARAMETER_NAME));
            }
        }

        // use private constructor to skip quality value validation step
        return new QualitySourceMediaType(type, subType, parameters, qs);
    }

    /**
     * Extract quality source information from the supplied {@link MediaType} value.
     *
     * If no quality source parameter is present in the media type, {@link Quality#DEFAULT
     * default quality} is returned.
     *
     * @param mediaType media type.
     * @return quality source parameter value or {@link Quality#DEFAULT default quality},
     * if no quality source parameter is present.
     *
     * @throws IllegalArgumentException in case the quality source parameter value could not be parsed.
     */
    public static int getQualitySource(final MediaType mediaType) throws IllegalArgumentException {
        if (mediaType instanceof QualitySourceMediaType) {
            return ((QualitySourceMediaType) mediaType).getQuality();
        } else {
            return getQs(mediaType);
        }
    }

    private static int getQs(MediaType mt) throws IllegalArgumentException {
        try {
            return getQs(mt.getParameters().get(Quality.QUALITY_SOURCE_PARAMETER_NAME));
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static int getQs(String v) throws ParseException {
        if (v == null) {
            return Quality.DEFAULT;
        }

        return HttpHeaderReader.readQualityFactor(v);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        if (obj instanceof QualitySourceMediaType) {
            final QualitySourceMediaType other = (QualitySourceMediaType) obj;
            return this.qs == other.qs;
        } else {
            // obj is a plain MediaType instance
            // with a quality source factor set to default (1.0)
            return this.qs == Quality.DEFAULT;
        }
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        return (this.qs == Quality.DEFAULT) ? hash : 47 * hash + this.qs;
    }

    @Override
    public String toString() {
        return "{" + super.toString() + ", qs=" + qs + "}";
    }
}
