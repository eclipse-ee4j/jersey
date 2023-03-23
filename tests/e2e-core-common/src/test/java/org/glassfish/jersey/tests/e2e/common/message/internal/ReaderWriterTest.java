/*
 * Copyright (c) 2012, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common.message.internal;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReaderWriterTest {
    @Test
    public void testToNeverAskToReadZeroBytes() throws IOException {
        // Unnamed app server bug test
        int size = ((ReaderWriter.BUFFER_SIZE + 1000) / 10) * 10;
        StringBuilder sb = new StringBuilder(size);
        String shortMsg = "0123456789";
        while (sb.length() < size) {
            sb.append(shortMsg);
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)) {
            @Override
            public synchronized int read(byte[] b, int off, int len) {
                if (len == 0) {
                    return -1; // simulate the bug
                }
                return super.read(b, off, len);
            }
        };

        String read = ReaderWriter.readFromAsString(byteArrayInputStream, MediaType.TEXT_HTML_TYPE);
        Assertions.assertEquals(size, read.length());
    }
}
