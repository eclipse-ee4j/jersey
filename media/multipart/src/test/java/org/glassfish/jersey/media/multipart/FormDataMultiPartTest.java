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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test case for {@link FormDataMultiPart}.
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class FormDataMultiPartTest extends MultiPartTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        multiPart = new FormDataMultiPart();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        multiPart = null;
        super.tearDown();
    }

    @Test
    @SuppressWarnings("empty-statement")
    public void testFieldsFDMP() throws Exception {
        FormDataMultiPart fdmp = (FormDataMultiPart) multiPart;
        assertEquals(0, fdmp.getFields().size());
        fdmp = fdmp.field("foo", "bar").field("baz", "bop");

        assertEquals(2, fdmp.getFields().size());

        assertNotNull(fdmp.getField("foo"));
        assertEquals("bar", fdmp.getField("foo").getValue());
        assertNotNull(fdmp.getField("baz"));
        assertEquals("bop", fdmp.getField("baz").getValue());

        assertEquals("bar", fdmp.getFields("foo").get(0).getValue());
        assertEquals("bop", fdmp.getFields("baz").get(0).getValue());

        assertNotNull(fdmp.getFields().get("foo"));
        assertEquals("bar", fdmp.getFields().get("foo").get(0).getValue());
        assertNotNull(fdmp.getFields().get("baz"));
        assertEquals("bop", fdmp.getFields().get("baz").get(0).getValue());


        fdmp = fdmp.field("foo", "bar").field("baz", "bop");

        assertEquals(2, fdmp.getFields().get("foo").size());
        assertEquals(2, fdmp.getFields().get("baz").size());
        assertEquals(2, fdmp.getFields("foo").size());
        assertEquals(2, fdmp.getFields("baz").size());
    }

}
