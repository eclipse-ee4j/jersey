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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case for {@link FormDataBodyPart}.
 *
 * @author Craig McClanahan
 * @author Imran M Yousuf (imran at smartitengineering.com)
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class FormDataBodyPartTest extends BodyPartTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        bodyPart = new FormDataBodyPart();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        bodyPart = null;
        super.tearDown();
    }

    @Test
    public void testCreateFDBP() throws Exception {
        FormDataBodyPart fdbp = (FormDataBodyPart) bodyPart;
        assertNull(fdbp.getFormDataContentDisposition());
        assertNull(fdbp.getName());
        assertNull(fdbp.getValue());
        assertTrue(fdbp.isSimple());

        fdbp = new FormDataBodyPart("<foo>bar</foo>", MediaType.APPLICATION_XML_TYPE);
        assertNull(fdbp.getFormDataContentDisposition());
        assertNull(fdbp.getName());

        try {
            fdbp.getValue();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected result.
        }

        assertEquals("<foo>bar</foo>", fdbp.getEntity());
        assertTrue(!fdbp.isSimple());

        fdbp = new FormDataBodyPart("name", "value");
        assertNotNull(fdbp.getFormDataContentDisposition());
        assertEquals("name", fdbp.getName());
        assertEquals("value", fdbp.getValue());
        assertTrue(fdbp.isSimple());

        fdbp = new FormDataBodyPart("name", "<foo>bar</foo>", MediaType.APPLICATION_XML_TYPE);
        assertNotNull(fdbp.getFormDataContentDisposition());
        assertEquals("name", fdbp.getName());

        try {
            fdbp.getValue();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected result.
        }

        assertEquals("<foo>bar</foo>", fdbp.getEntity());
        assertTrue(!fdbp.isSimple());

        fdbp = new FormDataBodyPart(
                FormDataContentDisposition.name("name").fileName("filename").build(),
                "<foo>bar</foo>",
                MediaType.APPLICATION_XML_TYPE);
        assertNotNull(fdbp.getFormDataContentDisposition());
        assertEquals("name", fdbp.getName());
        assertEquals("filename", fdbp.getFormDataContentDisposition().getFileName());

        try {
            fdbp.getValue();
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected result.
        }

        assertEquals("<foo>bar</foo>", fdbp.getEntity());
        assertTrue(!fdbp.isSimple());

        try {
            fdbp = new FormDataBodyPart();
            fdbp.setName(null);
            fail("Name should be null settable!");
        } catch (IllegalArgumentException argumentException) {
            // Expected result.
        }
    }

}
