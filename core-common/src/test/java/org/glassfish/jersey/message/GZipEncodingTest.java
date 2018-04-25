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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

/**
 * @author Martin Matula
 */
public class GZipEncodingTest extends AbstractEncodingTest {
    @Test
    public void testEncode() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new GZipEncoder().encode("gzip", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new GZIPInputStream(stream);
            }
        });
    }

    @Test
    public void testDecode() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new GZIPOutputStream(stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new GZipEncoder().decode("gzip", stream);
            }
        });
    }

    @Test
    public void testEncodeDecode() throws IOException {
        test(new TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new GZipEncoder().encode("gzip", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new GZipEncoder().decode("gzip", stream);
            }
        });
    }
}
