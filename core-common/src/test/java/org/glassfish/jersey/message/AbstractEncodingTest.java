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

package org.glassfish.jersey.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Super class for encoding tests - contains convenient way of defining the test by simply providing the encoding and
 * decoding streams.
 *
 * @author Martin Matula
 */
public class AbstractEncodingTest {
    /**
     * Main testing method that runs the test based on the passed test spec.
     *
     * @param testSpec Test-specific routines (providing encoding/decoding streams).
     * @throws IOException I/O exception.
     */
    protected void test(TestSpec testSpec) throws IOException {
        byte[] entity = "Hello world!".getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream encoded = testSpec.getEncoded(baos);
        encoded.write(entity);
        encoded.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        byte[] result = new byte[entity.length];
        InputStream decoded = testSpec.getDecoded(bais);
        int len = decoded.read(result);
        assertEquals(-1, decoded.read());
        decoded.close();
        assertEquals(entity.length, len);
        assertArrayEquals(entity, result);
    }

    /**
     * Interface that a test typically implements using an anonymous class to provide the test-specific functionality.
     */
    protected static interface TestSpec {
        /**
         * Returns encoded stream.
         *
         * @param stream Original stream.
         * @return Encoded stream.
         * @throws IOException I/O exception.
         */
        OutputStream getEncoded(OutputStream stream) throws IOException;

        /**
         * Returns decoded stream.
         *
         * @param stream Original stream.
         * @return Decoded stream.
         * @throws IOException I/O exception.
         */
        InputStream getDecoded(InputStream stream) throws IOException;
    }
}
