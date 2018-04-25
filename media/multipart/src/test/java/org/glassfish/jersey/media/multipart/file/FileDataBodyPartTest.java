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

package org.glassfish.jersey.media.multipart.file;

import java.io.File;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.BodyPartTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link FileDataBodyPart}.
 *
 * @author Imran M Yousuf (imran at smartitengineering.com)
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class FileDataBodyPartTest extends BodyPartTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        bodyPart = new FileDataBodyPart();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        bodyPart = null;
        super.tearDown();
    }

    @Override
    @Test
    public void testEntity() {
        try {
            bodyPart.setEntity("foo bar baz");
        } catch (UnsupportedOperationException exception) {
            // exception expected.
        }
    }

    @Test
    public void testCreateFDBP() throws Exception {
        FileDataBodyPart fdbp = (FileDataBodyPart) bodyPart;
        assertNull(fdbp.getFormDataContentDisposition());
        assertNull(fdbp.getName());
        assertNull(fdbp.getValue());
        assertTrue(fdbp.isSimple());
        String name;

        File file = new File("pom.xml");
        name = "xml";
        fdbp = new FileDataBodyPart(name, file);
        MediaType expectedType = MediaType.APPLICATION_XML_TYPE;
        checkEntityAttributes(name, fdbp, file, expectedType);
        fdbp.setName(name);
        checkEntityAttributes(name, fdbp, file, expectedType);
        fdbp.setFileEntity(file);
        checkEntityAttributes(name, fdbp, file, expectedType);

        fdbp = new FileDataBodyPart(name, file, expectedType);
        checkEntityAttributes(name, fdbp, file, expectedType);
        fdbp.setFileEntity(file, expectedType);
        checkEntityAttributes(name, fdbp, file, expectedType);

        file = new File("pom.png");
        name = "png";
        fdbp = new FileDataBodyPart("png", file);
        expectedType = DefaultMediaTypePredictor.CommonMediaTypes.PNG.getMediaType();
        checkEntityAttributes(name, fdbp, file, expectedType);

        file = new File("pom.zip");
        fdbp = new FileDataBodyPart(name, file);
        expectedType = DefaultMediaTypePredictor.CommonMediaTypes.ZIP.getMediaType();
        checkEntityAttributes(name, fdbp, file, expectedType);

        file = new File("pom.avi");
        fdbp = new FileDataBodyPart(name, file);
        expectedType = DefaultMediaTypePredictor.CommonMediaTypes.AVI.getMediaType();
        checkEntityAttributes(name, fdbp, file, expectedType);
    }

    private void checkEntityAttributes(final String name, final FileDataBodyPart fdbp, final File file,
                                       final MediaType expectedType) {
        if (name != null) {
            assertEquals(name, fdbp.getName());
            assertEquals(name, fdbp.getFormDataContentDisposition().getName());
            assertEquals(file.getName(), fdbp.getContentDisposition().getFileName());
            if (file.exists()) {
                assertEquals(file.length(), fdbp.getContentDisposition().getSize());
                assertEquals(file.lastModified(), fdbp.getContentDisposition().getModificationDate().getTime());
            } else {
                assertEquals(-1, fdbp.getContentDisposition().getSize());
            }
        } else {
            assertNull(fdbp.getName());
            assertNull(fdbp.getFormDataContentDisposition());
        }
        assertEquals(file, fdbp.getEntity());
        assertTrue(!fdbp.isSimple());
        assertEquals(expectedType, fdbp.getMediaType());
    }

}
