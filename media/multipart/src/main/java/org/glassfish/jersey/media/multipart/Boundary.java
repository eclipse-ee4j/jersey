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

package org.glassfish.jersey.media.multipart;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.MediaType;

/**
 * Utility for creating boundary parameters.
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public final class Boundary {

    public static final String BOUNDARY_PARAMETER = "boundary";

    private static final AtomicInteger boundaryCounter = new AtomicInteger();

    /**
     * Transforms a media type and add a boundary parameter with a unique value
     * if one is not already present.
     *
     * @param mediaType if {@code null} then a media type of "multipart/mixed" with a boundary parameter will be returned.
     * @return the media type with a boundary parameter.
     */
    public static MediaType addBoundary(MediaType mediaType) {
        if (mediaType == null) {
            return MultiPartMediaTypes.createMixed();
        }

        if (!mediaType.getParameters().containsKey(BOUNDARY_PARAMETER)) {
            final Map<String, String> parameters = new HashMap<String, String>(
                    mediaType.getParameters());
            parameters.put(BOUNDARY_PARAMETER, createBoundary());

            return new MediaType(mediaType.getType(), mediaType.getSubtype(),
                    parameters);
        }

        return mediaType;
    }

    /**
     * Creates a unique boundary.
     *
     * @return the boundary.
     */
    public static String createBoundary() {
        return new StringBuilder("Boundary_")
                .append(boundaryCounter.incrementAndGet())
                .append('_')
                .append(new Object().hashCode())
                .append('_')
                .append(System.currentTimeMillis())
                .toString();
    }

}
