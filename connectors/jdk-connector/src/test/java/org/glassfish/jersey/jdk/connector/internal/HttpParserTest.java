/*
 * Copyright (c) 2015, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Petr Janouch
 */
public class HttpParserTest {

    private static final Charset responseEncoding = Charset.forName("ISO-8859-1");

    private HttpParser httpParser;

    @BeforeEach
    public void prepare() {
        httpParser = new HttpParser(1000, 1000);
    }

    @Test
    public void testResponseLineInOnePiece() throws ParseException {
        testResponseLine(Integer.MAX_VALUE);
    }

    @Test
    public void testResponseLineSegmented() throws ParseException {
        testResponseLine(20);
    }

    private void testResponseLine(int segmentSize) throws ParseException {
        httpParser.reset(false);
        feedParser("HTTP/1.1 123 A meaningful code\r\n\r\n", segmentSize);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());

        HttpResponse httpResponse = httpParser.getHttpResponse();
        assertNotNull(httpResponse);

        assertEquals("HTTP/1.1", httpResponse.getProtocolVersion());
        assertEquals(123, httpResponse.getStatusCode());
        assertEquals("A meaningful code", httpResponse.getReasonPhrase());
    }

    @Test
    public void testHeadersInOnePiece() throws ParseException {
        testHeaders(Integer.MAX_VALUE);
    }

    @Test
    public void testHeadersSegmented() throws ParseException {
        testHeaders(20);
    }

    private void testHeaders(int segmentSize) throws ParseException {
        httpParser.reset(false);
        StringBuilder request = new StringBuilder();
        request.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("name1: value1\r\n")
                .append("name2: value2\r\n\r\n");
        feedParser(request.toString(), segmentSize);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());

        verifyHeaderValue("name1", "value1");
        verifyHeaderValue("name2", "value2");
    }

    private void verifyHeaderValue(String name, String... expectedValues) {
        verifyHeaderValue(name, false, expectedValues);
    }

    private void verifyTrailerHeaderValue(String name, String... expectedValues) {
        verifyHeaderValue(name, true, expectedValues);
    }

    private void verifyHeaderValue(String name, boolean trailerHeader, String... expectedValues) {
        HttpResponse httpResponse = httpParser.getHttpResponse();
        List<String> receivedValues;

        if (trailerHeader) {
            receivedValues = httpResponse.getTrailerHeader(name);
        } else {
            receivedValues = httpResponse.getHeader(name);
        }

        assertNotNull(receivedValues);
        assertEquals(expectedValues.length, receivedValues.size());
        for (String expectedValue : expectedValues) {
            assertTrue(receivedValues.contains(expectedValue));
        }
    }

    @Test
    public void testFixedLengthBodyInOnePiece() throws ParseException, IOException {
        testFixedLengthBody(Integer.MAX_VALUE);
    }

    @Test
    public void testFixedLengthBodySegmented() throws ParseException, IOException {
        testFixedLengthBody(20);
    }

    private void testFixedLengthBody(int segmentSize) throws ParseException, IOException {
        httpParser.reset(true);

        StringBuilder request = new StringBuilder();
        request.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("name1: value1\r\n")
                .append("name2: value2\r\n")
                .append("Content-Length: 56\r\n\r\n");

        StringBuilder bodyBuilder = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            bodyBuilder.append("ABCDEFG");
        }

        String body = bodyBuilder.toString();
        request.append(body);

        feedParser(request.toString(), segmentSize);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());

        verifyReceivedBody(body);
    }

    @Test
    public void testChunkedBodyInOnePiece() throws ParseException, IOException {
        testChunkedBody(Integer.MAX_VALUE, 25, generateBody());
    }

    @Test
    public void testChunkedBodySegmentedWithSmallChunk() throws ParseException, IOException {
        testChunkedBody(20, 15, generateBody());
    }

    @Test
    public void testChunkedBodySegmentedWithLargerChunk() throws ParseException, IOException {
        testChunkedBody(20, 23, generateBody());
    }

    @Test
    public void testEmptyChunkedBody() throws ParseException, IOException {
        testChunkedBody(Integer.MAX_VALUE, 25, "");
    }

    private void testChunkedBody(int segmentSize, int chunkSize, String responseBody) throws ParseException, IOException {
        httpParser.reset(true);

        StringBuilder request = new StringBuilder();
        request.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("name1: value1\r\n")
                .append("name2: value2\r\n")
                .append("Transfer-encoding: chunked\r\n\r\n");

        String chunkedBody = encodeChunk(responseBody, chunkSize, new HashMap<>());
        request.append(chunkedBody);

        feedParser(request.toString(), segmentSize);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());

        verifyReceivedBody(responseBody);
    }

    private String encodeChunk(String message, int chunkSize, Map<String, String> trailerHeaders)
            throws UnsupportedEncodingException {
        int messageLength = message.getBytes("ASCII").length;
        int chunkStartIdx = 0;

        StringBuilder body = new StringBuilder();
        while (chunkStartIdx < messageLength) {
            int chunkLength = chunkStartIdx + chunkSize < messageLength - 1 ? chunkSize : messageLength - chunkStartIdx;
            body.append(Integer.toHexString(chunkLength)).append("\r\n");
            body.append(message.substring(chunkStartIdx, chunkStartIdx + chunkLength));
            body.append("\r\n");
            chunkStartIdx += chunkLength;
        }

        body.append("0").append("\r\n");

        for (Map.Entry<String, String> header : trailerHeaders.entrySet()) {
            body.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        body.append("\r\n");

        return body.toString();
    }

    @Test
    public void testMultilineHeaderInOnePiece() throws ParseException {
        testMultilineHeader(Integer.MAX_VALUE);
    }

    @Test
    public void testMultilineHeaderSegmented() throws ParseException {
        testMultilineHeader(10);
    }

    private void testMultilineHeader(int segmentSize) throws ParseException {
        httpParser.reset(false);
        StringBuilder request = new StringBuilder();
        request.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("name1: value1\r\n")
                .append("multi-line: first\r\n          second\r\n       third\r\n")
                .append("name2: value2\r\n\r\n");
        feedParser(request.toString(), segmentSize);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());

        verifyHeaderValue("name1", "value1");
        verifyHeaderValue("name2", "value2");
        verifyHeaderValue("multi-line", "first second third");
    }

    @Test
    public void testMultilineHeaderNInOnePiece() throws ParseException {
        testMultilineHeaderN(Integer.MAX_VALUE);
    }

    @Test
    public void testMultilineHeaderNSegmented() throws ParseException {
        testMultilineHeaderN(10);
    }

    private void testMultilineHeaderN(int segmentSize) throws ParseException {
        httpParser.reset(false);
        StringBuilder request = new StringBuilder();
        request.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("name1: value1\r\n")
                .append("multi-line: first\n          second\n       third\r\n")
                .append("name2: value2\r\n\r\n");
        feedParser(request.toString(), segmentSize);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());

        verifyHeaderValue("name1", "value1");
        verifyHeaderValue("name2", "value2");
        verifyHeaderValue("multi-line", "first second third");
    }

    @Test
    public void testOverflowProtocol() {
        try {
            testOverflow("HTTP/1.0 404 Not found\n\n", 2);
            fail();
        } catch (ParseException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testOverflowCode() {
        try {
            testOverflow("HTTP/1.0 404 Not found\n\n", 11);
            fail();
        } catch (ParseException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testOverflowPhrase() {
        try {
            testOverflow("HTTP/1.0 404 Not found\n\n", 19);
            fail();
        } catch (ParseException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testOverflowHeader() {
        try {
            testOverflow("HTTP/1.0 404 Not found\nHeader1: somevalue\n\n", 30);
            fail();
        } catch (ParseException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testTrailerHeadersInOnePiece() throws IOException, ParseException {
        testTrailerHeaders(Integer.MAX_VALUE, 15);
    }

    @Test
    public void testTrailerHeadersSegmented() throws IOException, ParseException {
        testTrailerHeaders(20, 15);
    }

    @Test
    public void testSpacesInChunkSizeHeader() throws Exception {
        httpParser.reset(true);

        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("Transfer-Encoding: chunked\r\n\r\n");

        String body = "ABCDE";
        String bodyLen = Integer.toHexString(body.length());
        response.append("  ").append(bodyLen).append("  ").append("\r\n").append(body).append("\r\n");
        response.append("  0  ").append("\r\n").append("\r\n");

        feedParser(response.toString(), Integer.MAX_VALUE);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());
        verifyReceivedBody(body);
    }

    /**
     * This seems to be broken in Grizzly parser
     */
    @Disabled
    @Test
    public void testChunkExtension() throws ParseException, IOException {
        httpParser.reset(true);

        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("Transfer-Encoding: chunked\r\n\r\n");

        String body = "ABCDE";
        String bodyLen = Integer.toHexString(body.length());
        response.append(bodyLen).append(";extName=extValue").append("\r\n").append(body).append("\r\n");
        response.append("0;extName2=extValue2").append("\r\n").append("\r\n");

        feedParser(response.toString(), Integer.MAX_VALUE);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());
        verifyReceivedBody(body);
    }

    @Test
    public void testSameHeaders() throws ParseException {
        httpParser.reset(false);
        StringBuilder request = new StringBuilder();
        request.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("name1: value1\r\n")
                .append("name2: value2\r\n")
                .append("name3: value3\r\n")
                .append("name2: value4\r\n\r\n");
        feedParser(request.toString(), Integer.MAX_VALUE);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());

        verifyHeaderValue("name1", "value1");
        verifyHeaderValue("name2", "value2", "value2");
        verifyHeaderValue("name3", "value3");
    }

    @Test
    public void testSameHeadersCommaSeparated() throws ParseException {
        httpParser.reset(false);
        StringBuilder request = new StringBuilder();
        request.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("name1: value1\r\n")
                .append("name2: value2, value4\r\n")
                .append("name3: value3\r\n\r\n");
        feedParser(request.toString(), Integer.MAX_VALUE);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());

        verifyHeaderValue("name1", "value1");
        verifyHeaderValue("name2", "value2", "value4");
        verifyHeaderValue("name3", "value3");
    }

    @Test
    public void testInseparableHeaders() throws ParseException {
        httpParser.reset(false);
        StringBuilder request = new StringBuilder();
        request.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("name1: value1\r\n")
                .append("WWW-Authenticate: value2, value4\r\n")
                .append("name3: value3, value5\r\n\r\n");
        feedParser(request.toString(), Integer.MAX_VALUE);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());

        verifyHeaderValue("name1", "value1");
        verifyHeaderValue("WWW-Authenticate", "value2, value4");
        verifyHeaderValue("name3", "value3", "value5");
    }

    private void testTrailerHeaders(int segmentSize, int chunkSize) throws IOException, ParseException {
        httpParser.reset(true);

        StringBuilder request = new StringBuilder();
        request.append("HTTP/1.1 123 A meaningful code\r\n")
                .append("name1: value1\r\n")
                .append("name2: value2\r\n")
                .append("Transfer-Encoding: chunked\r\n\r\n");

        StringBuilder bodyBuilder = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            bodyBuilder.append("ABCDEFG");
        }

        String body = bodyBuilder.toString();

        Map<String, String> trailerHeaders = new HashMap<>();
        trailerHeaders.put("name3", "value3");
        trailerHeaders.put("name2", "value4");

        String chunkedBody = encodeChunk(body, chunkSize, trailerHeaders);
        request.append(chunkedBody);

        feedParser(request.toString(), segmentSize);

        assertTrue(httpParser.isHeaderParsed());
        assertTrue(httpParser.isComplete());

        verifyHeaderValue("name1", "value1");
        verifyHeaderValue("name2", "value2");
        verifyTrailerHeaderValue("name3", "value3");
        verifyTrailerHeaderValue("name2", "value4");

        verifyReceivedBody(body);
    }

    private void testOverflow(String response, int maxHeaderSize) throws ParseException {
        httpParser = new HttpParser(maxHeaderSize, Integer.MAX_VALUE);
        httpParser.reset(false);
        feedParser(response, Integer.MAX_VALUE);
    }

    private void verifyReceivedBody(String sentMessage) throws IOException {
        HttpResponse httpResponse = httpParser.getHttpResponse();
        AsynchronousBodyInputStream bodyStream = httpResponse.getBodyStream();

        byte[] receivedBytes = new byte[sentMessage.getBytes("ASCII").length];
        int writeIdx = 0;

        while (true) {
            byte b = (byte) bodyStream.read();
            if (b == (byte) -1) {
                break;
            }

            if (writeIdx == receivedBytes.length) {
                fail();
            }

            receivedBytes[writeIdx] = b;
            writeIdx++;
        }

        String receivedMessage = new String(receivedBytes, "ASCII");
        assertEquals(sentMessage, receivedMessage);
    }

    private void feedParser(String request, int segmentSize) throws ParseException {
        List<ByteBuffer> serializedResponse = new ArrayList<>();
        byte[] bytes = request.getBytes(responseEncoding);
        ByteBuffer bufferedResponse = ByteBuffer.wrap(bytes);
        int segmentStartIdx = 0;
        while (segmentStartIdx < bytes.length - 1) {
            int segmentLength = segmentStartIdx + segmentSize < bytes.length - 1 ? segmentSize : bytes.length - segmentStartIdx;
            byte[] segmentBytes = new byte[segmentLength];
            bufferedResponse.get(segmentBytes);
            ByteBuffer segment = ByteBuffer.wrap(segmentBytes);
            serializedResponse.add(segment);
            segmentStartIdx += segmentLength;
        }

        for (ByteBuffer input : serializedResponse) {
            httpParser.parse(input);
        }
    }

    private String generateBody() {
        StringBuilder bodyBuilder = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            bodyBuilder.append("ABCDEFG");
        }

        return bodyBuilder.toString();
    }
}
