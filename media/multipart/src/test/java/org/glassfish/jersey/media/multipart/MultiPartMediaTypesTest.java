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

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link MultiPartMediaTypes}.
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class MultiPartMediaTypesTest {

    @Test
    public void testMediaTypes() {
        test(MultiPartMediaTypes.MULTIPART_ALTERNATIVE_TYPE, MultiPartMediaTypes.createAlternative());
        test(MultiPartMediaTypes.MULTIPART_DIGEST_TYPE, MultiPartMediaTypes.createDigest());
        test(MultiPartMediaTypes.MULTIPART_MIXED_TYPE, MultiPartMediaTypes.createMixed());
        test(MultiPartMediaTypes.MULTIPART_PARELLEL_TYPE, MultiPartMediaTypes.createParallel());
        test(MediaType.MULTIPART_FORM_DATA_TYPE, MultiPartMediaTypes.createFormData());
    }

    private void test(MediaType x, MediaType y) {
        assertEquals(x.getType(), y.getType());
        assertEquals(x.getSubtype(), y.getSubtype());
        assertTrue(y.getParameters().containsKey("boundary"));
    }
}
