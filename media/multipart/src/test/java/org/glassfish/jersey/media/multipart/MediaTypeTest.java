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

package org.glassfish.jersey.media.multipart;

import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class MediaTypeTest {
    @Test
    public void testInputStreamDataMediaType() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream("test".getBytes());
        EntityPart entityPart = EntityPart.withName("textFile").fileName("test.txt")
                .content(bais)
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .build();
        Assert.assertEquals(MediaType.TEXT_PLAIN_TYPE, entityPart.getMediaType());
    }

    @Test
    public void testFileDataMediaType() throws IOException {
        EntityPart entityPart = EntityPart.withName("textFile")
                .content(new File("anyname"), File.class)
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .build();
        Assert.assertEquals(MediaType.TEXT_PLAIN_TYPE, entityPart.getMediaType());
    }

    @Test
    public void testGenericDataMediaType() throws IOException {
        EntityPart entityPart = EntityPart.withName("textFile")
                .content("Hello", String.class)
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .build();
        Assert.assertEquals(MediaType.TEXT_PLAIN_TYPE, entityPart.getMediaType());
    }

    @Test
    public void testInputStreamDataNullMediaType() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream("test".getBytes());
        EntityPart entityPart = EntityPart.withName("textFile").fileName("test.txt")
                .content(bais)
                .build();
        Assert.assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, entityPart.getMediaType());
    }

    @Test
    public void testGenericDataNullMediaType() throws IOException {
        EntityPart entityPart = EntityPart.withName("textFile")
                .content("Hello", String.class)
                .build();
        Assert.assertEquals(MediaType.TEXT_PLAIN_TYPE, entityPart.getMediaType());
    }
}
