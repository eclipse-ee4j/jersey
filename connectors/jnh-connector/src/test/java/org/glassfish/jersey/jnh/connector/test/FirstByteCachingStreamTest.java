/*
 * Copyright (c) 2023, 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector.test;

import org.glassfish.jersey.jnh.connector.JavaNetHttpConnector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;

class FirstByteCachingStreamTest {
    private static InputStream createFirstByteCachingStream(InputStream inner) throws Exception {
        Class[] classes = JavaNetHttpConnector.class.getDeclaredClasses();
        for (Class<?> clazz : classes) {
            if (clazz.getName().contains("FirstByteCachingStream")) {
                Constructor constructor = clazz.getDeclaredConstructor(InputStream.class);
                constructor.setAccessible(true);
                return (InputStream) constructor.newInstance(inner);
            }
        }
        throw new IllegalArgumentException("JavaNetHttpConnector inner class FirstByteCachingStream not found");
    }

    @Test
    void testNoByte() throws Exception {
        InputStream inputStream = InputStream.nullInputStream();
        InputStream testIs = createFirstByteCachingStream(inputStream);
        Assertions.assertEquals(0, testIs.available());
    }

    @Test
    void testOneByte() throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[]{'A'});
        InputStream testIs = createFirstByteCachingStream(byteArrayInputStream);
        Assertions.assertEquals(1, testIs.available());
        Assertions.assertEquals(1, testIs.available()); // idempotency
        Assertions.assertEquals('A', testIs.read());
        Assertions.assertEquals(0, testIs.available());
    }

    @Test
    void testOneByteInArray() throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[]{'A'});
        InputStream testIs = createFirstByteCachingStream(byteArrayInputStream);
        Assertions.assertEquals(1, testIs.available());

        byte[] bytes = new byte[1];
        int l = testIs.read(bytes);
        Assertions.assertEquals(1, l);
        Assertions.assertEquals('A', bytes[0]);
        Assertions.assertEquals(0, testIs.available());
    }

    @Test
    void testTwoBytes() throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[]{'A', 'B'});
        InputStream testIs = createFirstByteCachingStream(byteArrayInputStream);
        Assertions.assertEquals(2, testIs.available());
        Assertions.assertEquals(2, testIs.available()); // idempotency
        Assertions.assertEquals('A', testIs.read());
        Assertions.assertEquals(1, testIs.available());
        Assertions.assertEquals(1, testIs.available()); // idempotency
        Assertions.assertEquals('B', testIs.read());
        Assertions.assertEquals(0, testIs.available());
    }

    @Test
    void testTwoBytesReadAtOnce() throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[]{'A', 'B'});
        InputStream testIs = createFirstByteCachingStream(byteArrayInputStream);
        Assertions.assertEquals(2, testIs.available());

        byte[] bytes = new byte[2];
        int l = testIs.read(bytes);
        Assertions.assertEquals(2, l);
        Assertions.assertEquals('A', bytes[0]);
        Assertions.assertEquals('B', bytes[1]);
        Assertions.assertEquals(0, testIs.available());
    }
}
