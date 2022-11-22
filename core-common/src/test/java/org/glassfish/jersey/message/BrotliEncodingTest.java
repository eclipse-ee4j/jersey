/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.brotli.decoder.BrotliInputStream;
import com.oracle.brotli.encoder.BrotliOutputStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BrotliEncodingTest extends AbstractEncodingTest {

    @Test
    public void testEncode() throws IOException {
        test(new AbstractEncodingTest.TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new BrotliEncoder().encode("gzip", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return BrotliInputStream.builder().inputStream(stream).build();
            }
        });
    }

    @Test
    public void testDecode() throws IOException {
        test(new AbstractEncodingTest.TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return BrotliOutputStream.builder().outputStream(stream).build();
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new BrotliEncoder().decode("br", stream);
            }
        });
    }

    @Test
    public void testEncodeDecode() throws IOException {
        test(new AbstractEncodingTest.TestSpec() {
            @Override
            public OutputStream getEncoded(OutputStream stream) throws IOException {
                return new BrotliEncoder().encode("br", stream);
            }

            @Override
            public InputStream getDecoded(InputStream stream) throws IOException {
                return new BrotliEncoder().decode("br", stream);
            }
        });
    }
}
