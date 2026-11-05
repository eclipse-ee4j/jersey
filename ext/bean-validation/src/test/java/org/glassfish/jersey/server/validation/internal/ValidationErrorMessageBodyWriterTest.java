/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.jersey.server.validation.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;

import org.glassfish.jersey.server.validation.ValidationError;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that {@link ValidationErrorMessageBodyWriter} accepts media types with parameters, such as a charset.
 */
public class ValidationErrorMessageBodyWriterTest {

    private static final Annotation[] NO_ANNOTATIONS = new Annotation[0];

    private final ValidationErrorMessageBodyWriter writer = new ValidationErrorMessageBodyWriter();

    @Test
    public void testPlainTextWithCharsetIsWriteable() {
        assertTrue(isWriteable(MediaType.valueOf("text/plain;charset=UTF-8")));
    }

    @Test
    public void testHtmlWithCharsetIsWriteable() {
        assertTrue(isWriteable(MediaType.valueOf("text/html;charset=UTF-8")));
    }

    @Test
    public void testOtherMediaTypeIsNotWriteable() {
        assertFalse(isWriteable(MediaType.valueOf("text/xml;charset=UTF-8")));
    }

    @Test
    public void testPlainTextIsWrittenInTheRequestedCharset() throws IOException {
        final ValidationError error = new ValidationError("ne doit pas être nul",
                "{jakarta.validation.constraints.NotNull.message}", "Resource.get.arg0", "null");
        final ByteArrayOutputStream entityStream = new ByteArrayOutputStream();

        writer.writeTo(error, ValidationError.class, ValidationError.class, NO_ANNOTATIONS,
                MediaType.valueOf("text/plain;charset=UTF-16"), new MultivaluedHashMap<>(), entityStream);

        assertEquals("ne doit pas être nul (path = Resource.get.arg0, invalidValue = null)\n",
                entityStream.toString(StandardCharsets.UTF_16.name()));
    }

    private boolean isWriteable(final MediaType mediaType) {
        return writer.isWriteable(ValidationError.class, ValidationError.class, NO_ANNOTATIONS, mediaType);
    }
}
