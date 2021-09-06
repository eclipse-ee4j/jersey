/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved.
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
        final String fileName = "test.file";
        String fileNameExt;
        String encodedFilename;
        try {
            //incorrect fileNameExt - does not contain charset''
            try {
                fileNameExt = "testExt.file";
                assertFileNameExt(fileName, fileName, fileNameExt);
                fail("ParseException was expected to be thrown.");
            } catch (ParseException e) {
                //expected
            }

            //correct fileNameExt
            fileNameExt = "ISO-8859-1'language-us'abc%a1abc%a2%b1!#$&+.^_`|~-";
            assertFileNameExt(fileNameExt, fileName, fileNameExt);

            //correct fileNameExt
            fileNameExt = "UTF-8'language-us'abc%a1abc%a2%b1!#$&+.^_`|~-";
            assertFileNameExt(fileNameExt, fileName, fileNameExt);

            //correct fileNameExt
            fileNameExt = "UTF-8'us'fileName.txt";
            assertFileNameExt(fileNameExt, fileName, fileNameExt);

            //incorrect fileNameExt - too long language tag
            try {
                fileNameExt = "utf-8'languageTooLong'fileName.txt";
                assertFileNameExt(fileName, fileName, fileNameExt);
                fail("ParseException was expected to be thrown.");
            } catch (ParseException e) {
                //expected
            }

            //correct fileNameExt
            fileNameExt = "utf-8''a";
            assertFileNameExt(fileNameExt, fileName, fileNameExt);

            //incorrect fileNameExt - language tag does not match to pattern
            try {
                fileNameExt = "utf-8'lang-'a";
                assertFileNameExt(fileName, fileName, fileNameExt);
                fail("ParseException was expected to be thrown.");
            } catch (ParseException e) {
                //expected
            }

            //incorrect fileNameExt - ext-value contains an inappropriate symbol sequence (%z1). Jersey encodes it.
            fileNameExt = "utf-8'language-us'a%z1";
            encodedFilename = "utf-8'language-us'a%25z1";
            assertFileNameExt(encodedFilename, fileName, fileNameExt);

            //Incorrect fileNameExt - ext-value contains an inappropriate symbol sequence (%z1).
            //Jersey won't encodes it because of the unsupported charset.
            try {
                fileNameExt = "windows-1251'ru-ru'a%z1";
                assertFileNameExt(fileName, fileName, fileNameExt);
                fail("ParseException was expected to be thrown.");
            } catch (ParseException e) {
                //expected
            }

            //correct fileNameExt
            fileNameExt = "UTF-8'language-us'abc%a1abc%a2%b1";
            assertFileNameExt(fileNameExt, fileName, fileNameExt);

            //correct fileNameExt - encoded with other charset
            fileNameExt = "UTF-16'language-us'abc%a1abc%a2%b1";
            assertFileNameExt(fileNameExt, fileName, fileNameExt);

            //correct fileNameExt - unsupported charset, but fileName contains only valid characters
            fileNameExt = "Windows-1251'sr-Latn-RS'abc";
            assertFileNameExt(fileNameExt, fileName, fileNameExt);

            //correct fileNameExt
            fileNameExt = "utf-8'sr-Latn-RS'a";
            assertFileNameExt(fileNameExt, fileName, fileNameExt);

            //incorrect fileNameExt - ext-value contains % without two HEXDIG. Jersey encodes it.
            fileNameExt = "utf-8'language-us'a%";
            encodedFilename = "utf-8'language-us'a%25";
            assertFileNameExt(encodedFilename, fileName, fileNameExt);

            //correct fileNameExt
            fileNameExt = "UTF-8'language-us'abc.TXT";
            assertFileNameExt(fileNameExt, fileName, fileNameExt);

            //incorrect fileNameExt - no ext-value
            try {
                fileNameExt = "utf-8'language-us'";
                assertFileNameExt(fileName, fileName, fileNameExt);
                fail("ParseException was expected to be thrown.");
            } catch (ParseException e) {
                //expected
            }

            //incorrect fileNameExt - ext-value contains forbidden symbol (\). Jersey encodes it.
            fileNameExt = "utf-8'language-us'c:\\\\file.txt";
            encodedFilename = "utf-8'language-us'c%3A%5Cfile.txt";
            assertFileNameExt(encodedFilename, fileName, fileNameExt);

            //incorrect fileNameExt - ext-value contains forbidden symbol (/). Jersey encodes it.
            fileNameExt = "utf-8'language-us'home/file.txt";
            encodedFilename = "utf-8'language-us'home%2Ffile.txt";
            assertFileNameExt(encodedFilename, fileName, fileNameExt);

            //incorrect fileNameExt - ext-value contains forbidden symbol (李). Jersey encodes it.
            fileNameExt = "utf-8'language-us'李.txt";
            encodedFilename = "utf-8'language-us'%E6%9D%8E.txt";
            assertFileNameExt(encodedFilename, fileName, fileNameExt);

            //correct fileNameExt
            fileNameExt = "utf-8'language-us'FILEname.tXt";
            assertFileNameExt(fileNameExt, fileName, fileNameExt);

        } catch (ParseException ex) {
            fail(ex.getMessage());
        }
    }

    private void assertFileNameExt(
            final String expectedFileName,
            final String actualFileName,
            final String actualFileNameExt
    ) throws ParseException {
        final Date date = new Date();
        final String dateString = HttpDateFormat.getPreferredDateFormat().format(date);
        final String prefixHeader = contentDispositionType + ";filename=\"" + actualFileName + "\";"
                + "creation-date=\"" + dateString + "\";modification-date=\"" + dateString + "\";read-date=\""
                + dateString + "\";size=1222" + ";name=\"testData\";" + "filename*=\"";
        final String header = prefixHeader + actualFileNameExt + "\"";
        final ContentDisposition contentDisposition = new ContentDisposition(HttpHeaderReader.newInstance(header), true);
        assertEquals(expectedFileName, contentDisposition.getFileName());
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
