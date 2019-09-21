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

import java.util.Collections;

import javax.ws.rs.core.MediaType;

/**
 * Convenience {@link MediaType} (and associated String)
 * manifest constants.
 *
 * @author Craig McClanahan
 * @author Michal Gajdos
 */
public final class MultiPartMediaTypes {

    /** "multipart/alternative" */
    public static final String MULTIPART_ALTERNATIVE = "multipart/alternative";

    /** "multipart/alternative" */
    public static final MediaType MULTIPART_ALTERNATIVE_TYPE =
            new MediaType("multipart", "alternative");

    /** "multipart/digest" */
    public static final String MULTIPART_DIGEST = "multipart/digest";

    /** "multipart/digest" */
    public static final MediaType MULTIPART_DIGEST_TYPE =
            new MediaType("multipart", "digest");

    /** "multipart/mixed" */
    public static final String MULTIPART_MIXED = "multipart/mixed";

    /** "multipart/mixed" */
    public static final MediaType MULTIPART_MIXED_TYPE =
            new MediaType("multipart", "mixed");

    /** "multipart/parallel" */
    public static final String MULTIPART_PARALLEL = "multipart/parallel";

    /** "multipart/parallel" */
    public static final MediaType MULTIPART_PARELLEL_TYPE =
            new MediaType("multipart", "parallel");

    /**
     * @return a "multipart/alternative" with a boundary parameter.
     */
    public static MediaType createAlternative() {
        return create(MULTIPART_ALTERNATIVE_TYPE);
    }

    /**
     * @return a "multipart/digest" with a boundary parameter.
     */
    public static MediaType createDigest() {
        return create(MULTIPART_DIGEST_TYPE);
    }

    /**
     * @return a "multipart/mixed" with a boundary parameter.
     */
    public static MediaType createMixed() {
        return create(MULTIPART_MIXED_TYPE);
    }

    /**
     * @return a "multipart/parallel" with a boundary parameter.
     */
    public static MediaType createParallel() {
        return create(MULTIPART_PARELLEL_TYPE);
    }

    /**
     * @return a "multipart/form-data" with a boundary parameter.
     */
    public static MediaType createFormData() {
        return create(MediaType.MULTIPART_FORM_DATA_TYPE);
    }

    private static MediaType create(MediaType mt) {
        return new MediaType(mt.getType(), mt.getSubtype(),
                Collections.singletonMap(Boundary.BOUNDARY_PARAMETER, Boundary.createBoundary()));
    }

}
