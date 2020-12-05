/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.message.internal.HttpDateFormat;
import org.glassfish.jersey.message.internal.HttpHeaderReader;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Imran@SmartITEngineering.Com
 */
public class ContentDispositionTest {

    protected String contentDispositionType;

    public ContentDispositionTest() {
        contentDispositionType = "inline";
    }

    @Test
    public void testCreate() {
        ContentDisposition contentDisposition = ContentDisposition.type(null).build();

        assertNotNull(contentDisposition);
        assertEquals(null, contentDisposition.getType());

        contentDisposition = ContentDisposition.type(contentDispositionType).build();

        assertNotNull(contentDisposition);
        assertEquals(contentDispositionType, contentDisposition.getType());

        final Date date = new Date();
        contentDisposition = ContentDisposition.type(contentDispositionType).fileName("test.file")
                .creationDate(date).modificationDate(date).readDate(date).size(1222).build();

        assertContentDisposition(contentDisposition, date);
        String header = contentDispositionType;

        try {
            contentDisposition = new ContentDisposition(contentDisposition.toString());
            assertNotNull(contentDisposition);
            contentDisposition = new ContentDisposition(header);
            assertNotNull(contentDisposition);
            assertEquals(contentDispositionType, contentDisposition.getType());
            final String dateString = HttpDateFormat.getPreferredDateFormat().format(date);
            header = contentDispositionType + ";filename=\"test.file\";creation-date=\""
                    + dateString + "\";modification-date=\"" + dateString + "\";read-date=\""
                    + dateString + "\";size=1222";

            contentDisposition = new ContentDisposition(header);
            assertContentDisposition(contentDisposition, date);
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertContentDisposition(contentDisposition, date);
        } catch (final ParseException ex) {
            fail(ex.getMessage());
        }
        try {
            new ContentDisposition((HttpHeaderReader) null, true);
            fail("NullPointerException was expected to be thrown.");
        } catch (final ParseException exception) {
            fail(exception.getMessage());
        } catch (final NullPointerException exception) {
            //expected
        }
    }

    @Test
    public void testToString() {
        final Date date = new Date();
        final ContentDisposition contentDisposition = ContentDisposition.type(contentDispositionType).fileName("test.file")
                .creationDate(date).modificationDate(date).readDate(date).size(1222).build();
        final String dateString = HttpDateFormat.getPreferredDateFormat().format(date);
        final String header = contentDispositionType + "; filename=\"test.file\"; creation-date=\""
                + dateString + "\"; modification-date=\"" + dateString + "\"; read-date=\"" + dateString + "\"; size=1222";
        assertEquals(header, contentDisposition.toString());
    }

    @Test
    public void testFileNameExt() {
        final Date date = new Date();
        final String dateString = HttpDateFormat.getPreferredDateFormat().format(date);
        final String fileName = "test.file";

        try {
            //correct fileNameExt
            String fileNameExt = "testExt.file";
            final String prefixHeader = contentDispositionType + ";filename=\"" + fileName + "\";"
                    + "creation-date=\"" + dateString + "\";modification-date=\"" + dateString + "\";read-date=\""
                    + dateString + "\";size=1222" + ";name=\"testData\";" + "filename*=\"";
            String header = prefixHeader + fileNameExt + "\"";
            ContentDisposition contentDisposition =
                    new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileName, contentDisposition.getFileName());

            //correct fileNameExt
            fileNameExt = "ISO-8859-1'language-us'abc%a1abc%a2%b1!#$&+.^_`|~-";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileNameExt, contentDisposition.getFileName());

            //correct fileNameExt
            fileNameExt = "UTF-8'us'fileName.txt";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileNameExt, contentDisposition.getFileName());

            //incorrect fileNameExt
            fileNameExt = "utf-8'languageTooLong'fileName.txt";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileName, contentDisposition.getFileName());

            //correct fileNameExt
            fileNameExt = "utf-8''a";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileNameExt, contentDisposition.getFileName());

            //incorrect fileNameExt
            fileNameExt = "utf-8'lang-'a";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileName, contentDisposition.getFileName());

            //correct fileNameExt
            fileNameExt = "ISO-8859-1'language-us'a";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileNameExt, contentDisposition.getFileName());

            //correct fileNameExt
            fileNameExt = "ISO-8859-1'language-us'a%a1";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileNameExt, contentDisposition.getFileName());

            //incorrect fileNameExt
            fileNameExt = "ISO-8859-1'language-us'a%z1";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileName, contentDisposition.getFileName());

            //correct fileNameExt
            fileNameExt = "ISO-8859-1'language-us'abc%a1abc%a2%b1";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileNameExt, contentDisposition.getFileName());

            //incorrect fileNameExt
            fileNameExt = "ISO-8859-1'language-us'a%";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileName, contentDisposition.getFileName());

            //correct fileNameExt
            fileNameExt = "ISO-8859-1'language-us'abc%a1abc%a2%b1!#$&+.^_`|~-";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileNameExt, contentDisposition.getFileName());

            //correct fileNameExt
            fileNameExt = "ISO-8859-1'language-us'abc%a1abc%a2%b1!#$&+.^_`|~-";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileNameExt, contentDisposition.getFileName());

            //correct fileNameExt
            fileNameExt = "iso-8859-1'language-us'abc.TXT";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileNameExt, contentDisposition.getFileName());

            //incorrect fileNameExt
            fileNameExt = "ISO-8859-1'language-us'";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileName, contentDisposition.getFileName());

            //incorrect fileNameExt
            fileNameExt = "ISO-8859-1'language-us'c:\\file.txt";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileName, contentDisposition.getFileName());

            //incorrect fileNameExt
            fileNameExt = "ISO-8859-1'language-us'home/file.txt";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileName, contentDisposition.getFileName());

            //incorrect fileNameExt
            fileNameExt = "ISO-8859-1'language-us'李.txt";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileName, contentDisposition.getFileName());

            //correct fileNameExt
            fileNameExt = "ISO-8859-1'language-us'FILEname.tXt";
            header = prefixHeader + fileNameExt + "\"";
            contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
            assertEquals(fileNameExt, contentDisposition.getFileName());

        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

    protected void assertContentDisposition(final ContentDisposition contentDisposition, Date date) {
        assertNotNull(contentDisposition);
        assertEquals(contentDispositionType, contentDisposition.getType());
        assertEquals("test.file", contentDisposition.getFileName());
        assertEquals(date.toString(), contentDisposition.getModificationDate().toString());
        assertEquals(date.toString(), contentDisposition.getReadDate().toString());
        assertEquals(date.toString(), contentDisposition.getCreationDate().toString());
        assertEquals(1222, contentDisposition.getSize());
    }

}
