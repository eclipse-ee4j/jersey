/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.HEADERS_ONLY;
import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_ANY;
import static org.glassfish.jersey.logging.LoggingFeature.Verbosity.PAYLOAD_TEXT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;

/**
 * @author Ondrej Kosatka (ondrej.kosatka at oracle.com)
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

}
