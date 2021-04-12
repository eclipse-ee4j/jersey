/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.logging;

import org.mockito.stubbing.Answer;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.HEADERS_ONLY;
import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_ANY;
import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;

/**
 * @author Ondrej Kosatka
 */
public class LoggingInterceptorTest {

    //
    // isReadable
    //

    @Test
    public void testReadableTypeTestSubWild() {
        assertTrue(LoggingInterceptor.isReadable(new MediaType("text", "*")));
    }

    @Test
    public void testReadableTypeTestSubSomething() {
        assertTrue(LoggingInterceptor.isReadable(new MediaType("text", "something")));
    }

    @Test
    public void testReadableTypeAppSubJson() {
        assertTrue(LoggingInterceptor.isReadable(new MediaType("application", "json")));
    }

    @Test
    public void testReadableTypeApplicationSubVndApiJson() {
        assertTrue(LoggingInterceptor.isReadable(new MediaType("application", "vnd.api+json")));
    }

    @Test
    public void testReadableTypeAppSubBinary() {
        assertFalse(LoggingInterceptor.isReadable(new MediaType("application", "octet-stream")));
    }

    @Test
    public void testReadableTypeAppSubUnknown() {
        assertFalse(LoggingInterceptor.isReadable(new MediaType("application", "unknown")));
    }

    @Test
    public void testReadableTypeUnknownSubUnknown() {
        assertFalse(LoggingInterceptor.isReadable(new MediaType("unknown", "unknown")));
    }

    //
    // printEntity
    //

    @Test
    public void testVerbosityTextPrintTextEntity() {
        assertTrue(LoggingInterceptor.printEntity(PAYLOAD_TEXT, TEXT_HTML_TYPE));
    }

    @Test
    public void testVerbosityTextPrintBinaryEntity() {
        assertFalse(LoggingInterceptor.printEntity(PAYLOAD_TEXT, APPLICATION_OCTET_STREAM_TYPE));
    }

    @Test
    public void testVerbosityAnyPrintTextEntity() {
        assertTrue(LoggingInterceptor.printEntity(PAYLOAD_ANY, TEXT_HTML_TYPE));
    }

    @Test
    public void testVerbosityAnyPrintBinaryEntity() {
        assertTrue(LoggingInterceptor.printEntity(PAYLOAD_ANY, APPLICATION_OCTET_STREAM_TYPE));
    }

    @Test
    public void testVerbosityHeadersPrintTextEntity() {
        assertFalse(LoggingInterceptor.printEntity(HEADERS_ONLY, TEXT_HTML_TYPE));
    }

    @Test
    public void testVerbosityHeadersPrintBinaryEntity() {
        assertFalse(LoggingInterceptor.printEntity(HEADERS_ONLY, APPLICATION_OCTET_STREAM_TYPE));
    }

    //
    // logInboundEntity
    //

    @Test
    public void testLogInboundEntityMockedStream() throws Exception {
        int maxEntitySize = 20;
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor(LoggingFeature.builder().maxEntitySize(maxEntitySize)) {};

        StringBuilder buffer = new StringBuilder();
        InputStream stream = mock(InputStream.class);
        when(stream.markSupported()).thenReturn(true);

        when(stream.read(any(), eq(0), eq(maxEntitySize + 1)))
                .thenAnswer(chunk(4, 'a'));
        when(stream.read(any(), eq(4), eq(maxEntitySize + 1 - 4)))
                .thenAnswer(chunk(3, 'b'));
        when(stream.read(any(), eq(7), eq(maxEntitySize + 1 - 7)))
                .thenAnswer(chunk(5, 'c'));
        when(stream.read(any(), eq(12), eq(maxEntitySize + 1 - 12)))
                .thenReturn(-1);

        loggingInterceptor.logInboundEntity(buffer, stream, StandardCharsets.UTF_8);

        assertEquals("aaaabbbccccc\n", buffer.toString());
        verify(stream).mark(maxEntitySize + 1);
        verify(stream).reset();
    }

    private Answer<?> chunk(int size, char filler) {
        return invocation -> {
            byte[] buf = invocation.getArgumentAt(0, byte[].class);
            int offset = invocation.getArgumentAt(1, Integer.class);
            Arrays.fill(buf, offset, offset + size, (byte) filler);
            return size;
        };
    }

    @Test
    public void testLogInboundEntityRealStream() throws Exception {
        int maxEntitySize = 2000;
        String inputString = getRandomString(maxEntitySize * 2);

        LoggingInterceptor loggingInterceptor = new LoggingInterceptor(LoggingFeature.builder().maxEntitySize(maxEntitySize)) {};
        StringBuilder buffer = new StringBuilder();
        InputStream stream = new ByteArrayInputStream(inputString.getBytes());

        loggingInterceptor.logInboundEntity(buffer, stream, StandardCharsets.UTF_8);

        assertEquals(inputString.substring(0, maxEntitySize) + "...more...\n", buffer.toString());
    }

    private static String getRandomString(int length) {
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890 _";
        StringBuilder result = new StringBuilder();

        while (length > 0) {
            Random rand = new Random();
            result.append(characters.charAt(rand.nextInt(characters.length())));
            length--;
        }
        return result.toString();
    }
}
