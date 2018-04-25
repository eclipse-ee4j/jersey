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

package org.glassfish.jersey.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests several parsing use-cases of ChunkedInput
 *
 * @author Petr Bouda
 **/
public class FixedBoundaryParserTest {

    public static final String DELIMITER_4 = "1234";

    public static final String DELIMITER_1 = "#";

    @Test
    public void testFixedBoundaryParserNullInput() throws IOException {
        final ChunkParser parser = ChunkedInput.createParser(DELIMITER_4);
        InputStream input = new ByteArrayInputStream(new byte[] {});
        assertNull(parser.readChunk(input));
    }

    @Test
    public void testFixedBoundaryParserDelimiter4() throws IOException {
        final ChunkParser parser = ChunkedInput.createParser(DELIMITER_4);

        // delimiter is the same char sequence as an input
        assertNull(parse(parser, DELIMITER_4));

        // input starts with the delimiter
        assertEquals("123", parse(parser, DELIMITER_4 + "123"));

        // beginning of the input and delimiter are not the same
        assertEquals("abc", parse(parser, "abc" + DELIMITER_4 + "def"));

        // delimiter in the input is not complete, only partial
        assertEquals("abc123", parse(parser, "abc123"));

        // delimiter in the input is not complete, only partial,
        // and then continue with a char which is not part of the
        // delimiter
        assertEquals("abc1235", parse(parser, "abc1235"));

        // delimiter in the input is not complete, only partial,
        // and then continue with a char which is part of the
        // delimiter
        assertEquals("abc1231", parse(parser, "abc1231"));

        // input has the same beginning as the delimiter
        assertEquals("12", parse(parser, "121234"));

        // input ends with first char of delimiter
        assertEquals("1231", parse(parser, "1231"));
    }

    @Test
    public void testFixedBoundaryParserDelimiter1() throws IOException {
        final ChunkParser parser = ChunkedInput.createParser(DELIMITER_1);

        // delimiter is the same char sequence as an input
        assertNull(parse(parser, DELIMITER_1));

        // input starts with the delimiter
        assertEquals("123", parse(parser, DELIMITER_1 + "123"));

        // beginning of the input and delimiter are not the same
        assertEquals("abc", parse(parser, "abc" + DELIMITER_1 + "def"));

        // delimiter in the input is not complete, only partial
        assertEquals("abc123", parse(parser, "abc123"));
    }

    @Test
    public void delimiterWithRepeatedInitialCharacters() throws IOException {
        ChunkParser parser = ChunkedInput.createParser("**b**");
        assertEquals("1*", parse(parser, "1***b**"));
    }

    private static String parse(ChunkParser parser, String str) throws IOException {
        InputStream input = new ByteArrayInputStream(str.getBytes());
        byte[] bytes = parser.readChunk(input);
        return bytes == null ? null : new String(bytes);
    }

    @Test
    public void testFixedBoundaryParserFlow() throws IOException {
        final ChunkParser parser = ChunkedInput.createParser(DELIMITER_4);

        String input = "abc" + DELIMITER_4 + "edf" + DELIMITER_4 + "ghi";
        InputStream stream = new ByteArrayInputStream(input.getBytes());

        byte[] bytes = parser.readChunk(stream);
        assertEquals("abc", new String(bytes));

        bytes = parser.readChunk(stream);
        assertEquals("edf", new String(bytes));

        bytes = parser.readChunk(stream);
        assertEquals("ghi", new String(bytes));
    }

    @Test
    public void testFixedBoundaryParserFlowDelimiterFirst() throws IOException {
        final ChunkParser parser = ChunkedInput.createParser(DELIMITER_4);

        String input = DELIMITER_4 + "edf" + DELIMITER_4 + "ghi";
        InputStream stream = new ByteArrayInputStream(input.getBytes());

        byte[] bytes = parser.readChunk(stream);
        assertEquals("edf", new String(bytes));

        bytes = parser.readChunk(stream);
        assertEquals("ghi", new String(bytes));
    }

    @Test
    public void testFixedBoundaryParserFlowDelimiterEnds() throws IOException {
        final ChunkParser parser = ChunkedInput.createParser(DELIMITER_4);

        String input = "abc" + DELIMITER_4 + "edf" + DELIMITER_4;
        InputStream stream = new ByteArrayInputStream(input.getBytes());

        byte[] bytes = parser.readChunk(stream);
        assertEquals("abc", new String(bytes));

        bytes = parser.readChunk(stream);
        assertEquals("edf", new String(bytes));
    }

}
