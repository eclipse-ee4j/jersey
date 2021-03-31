/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message.internal;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UtilsTest {

    @Test
    public void createTempFile() throws IOException {
        final File file = Utils.createTempFile();
        final OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));

        try {
            final ByteArrayInputStream entityStream = new ByteArrayInputStream("Test stream byte input".getBytes());
            ReaderWriter.writeTo(entityStream, stream);
        } finally {
            stream.close();
        }
        Assert.assertTrue(file.exists());
    }

}
