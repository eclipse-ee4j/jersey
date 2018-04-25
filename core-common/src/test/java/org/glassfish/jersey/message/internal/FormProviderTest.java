/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * {@link FormProvider} unit tests
 *
 * @author Petr Bouda
 */
public class FormProviderTest {

    private static final FormProvider PROVIDER = new FormProvider();

    @Test
    public void testReadFormParam() {
        Form form = readFrom("name&age=26");
        MultivaluedMap<String, String> map = form.asMap();
        assertEquals(2, map.size());

        List<String> nameEntry = map.get("name");
        assertEquals(1, nameEntry.size());
        assertNull(nameEntry.get(0));

        List<String> ageEntry = map.get("age");
        assertEquals(1, ageEntry.size());
        assertEquals("26", ageEntry.get(0));
    }

    @Test
    public void testReadMultipleSameFormParam() {
        Form form = readFrom("name&name=George");
        MultivaluedMap<String, String> map = form.asMap();
        assertEquals(1, map.size());

        List<String> nameEntry = map.get("name");
        assertEquals(2, nameEntry.size());
        assertNull(nameEntry.get(0));
        assertEquals("George", nameEntry.get(1));
    }

    private static Form readFrom(String body) {
        try {
            InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
            return PROVIDER.readFrom(Form.class, Form.class, new Annotation[] {},
                    MediaType.APPLICATION_FORM_URLENCODED_TYPE, null, stream);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
}
