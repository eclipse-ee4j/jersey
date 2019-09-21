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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test case for {@link MultiPart}.
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class MultiPartTest {

    protected MultiPart multiPart = null;

    @Before
    public void setUp() throws Exception {
        multiPart = new MultiPart();
    }

    @After
    public void tearDown() throws Exception {
        multiPart = null;
    }

    @Test
    @SuppressWarnings("empty-statement")
    public void testCreate() {
        if (multiPart instanceof FormDataMultiPart) {
            assertEquals("multipart/form-data", multiPart.getMediaType().toString());
            try {
                multiPart.setMediaType(new MediaType("multipart", "foo"));
                fail("Should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // Expected result.
            }
            multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        } else {
            assertEquals("multipart/mixed", multiPart.getMediaType().toString());
            multiPart.setMediaType(new MediaType("multipart", "alternative"));
            assertEquals("multipart/alternative", multiPart.getMediaType().toString());
            try {
                multiPart.setMediaType(new MediaType("text", "xml"));
                fail("Should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // Expected result.
            }
        }
    }

    @Test
    @SuppressWarnings("empty-statement")
    public void testEntity() {
        try {
            multiPart.setEntity("foo bar baz");
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected result.
        }
        try {
            assertEquals("foo bar baz", multiPart.getEntity());
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected result.
        }
    }

}
