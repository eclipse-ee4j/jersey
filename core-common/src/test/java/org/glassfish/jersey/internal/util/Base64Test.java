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

package org.glassfish.jersey.internal.util;

import java.util.Arrays;
import java.util.Base64;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Martin Matula
 */
public class Base64Test {

    private static String[] decoded = new String[] {
            "any carnal pleasure.",
            "any carnal pleasure",
            "any carnal pleasur",
            "any carnal pleasu",
            "any carnal pleas"
    };

    private static String[] encoded = new String[] {
            "YW55IGNhcm5hbCBwbGVhc3VyZS4=",
            "YW55IGNhcm5hbCBwbGVhc3VyZQ==",
            "YW55IGNhcm5hbCBwbGVhc3Vy",
            "YW55IGNhcm5hbCBwbGVhc3U=",
            "YW55IGNhcm5hbCBwbGVhcw=="
    };

    @Test
    public void testEncodeString() throws Exception {
        for (int i = 0; i < decoded.length; i++) {
            assertEquals(encoded[i], new String(Base64.getEncoder().encode(decoded[i].getBytes("ASCII")), "ASCII"));
        }
    }

    @Test
    public void testDecodeString() throws Exception {
        for (int i = 0; i < encoded.length; i++) {
            assertEquals(decoded[i], new String(Base64.getDecoder().decode(encoded[i].getBytes("ASCII")), "ASCII"));
        }
    }

    @Test
    public void testRoundtripLengthMod3Equals0() {
        byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        byte[] result = Base64.getDecoder().decode(Base64.getEncoder().encode(data));
        assertTrue("failed to roundtrip value to base64", Arrays.equals(data, result));
    }

    @Test
    public void testRoundtripLengthMod3Equals1() {
        byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] result = Base64.getDecoder().decode(Base64.getEncoder().encode(data));
        assertTrue("failed to roundtrip value to base64", Arrays.equals(data, result));
    }

    @Test
    public void testRoundtripLengthMod3Equals2() {
        byte[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        byte[] result = Base64.getDecoder().decode(Base64.getEncoder().encode(data));
        assertTrue("failed to roundtrip value to base64", Arrays.equals(data, result));
    }

    @Test
    public void testRoundtripOneByteGreaterThan127() {
        byte[] data = {(byte) 128};
        try {
            byte[] result = Base64.getDecoder().decode(Base64.getEncoder().encode(data));
            // ok
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testRoundtripAssortedValues() {
        byte[] data = {0, 1, 63, 64, 65, (byte) 127, (byte) 128, (byte) 1299, (byte) 254, (byte) 255};
        try {
            Base64.getDecoder().decode(Base64.getEncoder().encode(data));
            // ok
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testEncodeByteArray() {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; ++i) {
            data[i] = (byte) (255 - i);
        }
        try {
            new String(Base64.getEncoder().encode(data));
            // ok
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testDecodeString2() {
        String data = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0"
                + "+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6e3x9fn"
                + "+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmqq6ytrq+wsbKztLW2t7i5uru8vb6"
                + "/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w==";
        byte[] result = Base64.getDecoder().decode(data.getBytes());

        assertEquals("incorrect length", result.length, 256);
        for (int i = 0; i < 256; ++i) {
            assertEquals("incorrect value", result[i], (byte) i);
        }
    }
}
