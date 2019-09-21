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

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link BodyPart}.
 *
 * @author Craig McClanahan
 * @author Imran M Yousuf (imran at smartitengineering.com)
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class BodyPartTest {

    @Before
    public void setUp() throws Exception {
        bodyPart = new BodyPart();
    }

    @After
    public void tearDown() throws Exception {
        bodyPart = null;
    }

    protected BodyPart bodyPart = null;

    @Test
    public void testCreate() {
        assertEquals("text/plain", bodyPart.getMediaType().toString());
        bodyPart.setMediaType(new MediaType("application", "json"));
        assertEquals("application/json", bodyPart.getMediaType().toString());
    }

    @Test
    public void testEntity() {
        bodyPart.setEntity("foo bar baz");
        assertEquals("foo bar baz", bodyPart.getEntity());
    }

    @Test
    public void testHeaders() {
        MultivaluedMap<String, String> headers = bodyPart.getHeaders();
        assertNotNull(headers);
        assertNull(headers.get(HttpHeaders.ACCEPT));
        headers.add(HttpHeaders.ACCEPT, "application/xml");
        assertEquals("application/xml", headers.getFirst(HttpHeaders.ACCEPT));
        headers.add(HttpHeaders.ACCEPT, "application/json");
        assertEquals("application/xml", headers.getFirst(HttpHeaders.ACCEPT));
        List values = headers.get(HttpHeaders.ACCEPT);
        assertTrue(values.contains("application/xml"));
        assertTrue(values.contains("application/json"));
        assertNotNull(headers.get("accept"));
        assertNotNull(headers.get("ACCEPT"));
    }

}
