/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.api;

import java.text.ParseException;
import java.util.Date;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.message.internal.HttpDateFormat;
import org.glassfish.jersey.message.internal.HttpHeaderReader;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Imran@SmartITEngineering.Com
 */
public class FormDataContentDispositionTest extends ContentDispositionTest {

    public FormDataContentDispositionTest() {
        contentDispositionType = "form-data";
    }

    @Test
    @Override
    public void testCreate() {
        final Date date = new Date();
        FormDataContentDisposition contentDisposition;
        contentDisposition = FormDataContentDisposition.name("testData").fileName("test.file").creationDate(date)
                .modificationDate(date).readDate(date).size(1222).build();
        assertFormDataContentDisposition(contentDisposition, date);
        try {
            final String dateString = HttpDateFormat.getPreferredDateFormat().format(date);
            final String header = contentDispositionType + ";filename=\"test.file\";creation-date=\"" + dateString
                    + "\";modification-date=\"" + dateString + "\";read-date=\"" + dateString + "\";size=1222"
                    + ";name=\"testData\"";

            contentDisposition = new FormDataContentDisposition(contentDisposition.toString());
            assertFormDataContentDisposition(contentDisposition, date);
            contentDisposition = new FormDataContentDisposition(header);
            assertFormDataContentDisposition(contentDisposition, date);
            contentDisposition = new FormDataContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertFormDataContentDisposition(contentDisposition, date);
        } catch (final ParseException ex) {
            fail(ex.getMessage());
        }
        try {
            new FormDataContentDisposition((HttpHeaderReader) null, true);
            fail("NullPointerException was expected to be thrown.");
        } catch (final ParseException exception) {
            fail(exception.getMessage());
        } catch (final NullPointerException exception) {
            //expected
        }
        try {
            new FormDataContentDisposition("form-data;filename=\"test.file\"");
            fail("IllegalArgumentException was expected to be thrown.");
        } catch (final ParseException exception) {
            fail(exception.getMessage());
        } catch (final IllegalArgumentException exception) {
            //expected
        }
        try {
            FormDataContentDisposition.name(null).build();
            fail("IllegalArgumentException was expected to be thrown.");
        } catch (final IllegalArgumentException exception) {
            //expected
        } catch (final Exception exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    @Override
    public void testToString() {
        final Date date = new Date();
        final FormDataContentDisposition contentDisposition = FormDataContentDisposition.name("testData")
                .fileName("test.file").creationDate(date).modificationDate(date)
                        .readDate(date).size(1222).build();
        final String dateString = HttpDateFormat.getPreferredDateFormat().format(date);
        final String header = contentDispositionType + "; filename=\"test.file\"; creation-date=\"" + dateString
                + "\"; modification-date=\"" + dateString + "\"; read-date=\"" + dateString + "\"; size=1222"
                + "; name=\"testData\"";

        assertEquals(header, contentDisposition.toString());
    }

    protected void assertFormDataContentDisposition(final FormDataContentDisposition contentDisposition, final Date date) {
        assertContentDisposition(contentDisposition, date);
        assertEquals("testData", contentDisposition.getName());
    }

}
