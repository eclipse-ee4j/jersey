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

package org.glassfish.jersey.media.multipart;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.media.multipart.internal.LocalizationMessages;

import org.jvnet.mimepull.MIMEPart;

/**
 * Proxy class representing the entity of a {@link BodyPart} when a
 * {@link MultiPart} entity is received and parsed.
 * <p/>
 * Its primary purpose is to provide an input stream to retrieve the actual data.
 * However, it also transparently deals with storing the data in a temporary disk
 * file, if it is larger than a configurable size; otherwise, the data is stored
 * in memory for faster processing.
 *
 * @author Craig McClanahan
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class BodyPartEntity implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(BodyPartEntity.class.getName());

    private final MIMEPart mimePart;
    private volatile File file;

    /**
     * Constructs a new {@code BodyPartEntity} with a {@link MIMEPart}.
     *
     * @param mimePart MIMEPart containing the input stream of this body part entity.
     */
    public BodyPartEntity(final MIMEPart mimePart) {
        this.mimePart = mimePart;
    }

    /**
     * Gets the input stream of the raw bytes of this body part entity.
     *
     * @return the input stream of the body part entity.
     */
    public InputStream getInputStream() {
        return mimePart.read();
    }

    /**
     * Cleans up temporary file(s), if any were utilized.
     */
    public void cleanup() {
        mimePart.close();

        if (file != null) {
            final boolean deleted = file.delete();
            if (!deleted) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, LocalizationMessages.TEMP_FILE_NOT_DELETED(file.getAbsolutePath()));
                }
            }
        }
    }

    /**
     * Defers to {@link #cleanup}.
     */
    public void close() throws IOException {
        cleanup();
    }

    /**
     * Move the contents of the underlying {@link java.io.InputStream} or {@link java.io.File} to the given file.
     *
     * @param file destination file.
     */
    public void moveTo(final File file) {
        mimePart.moveTo(file);

        // Remember the file where the mime-part object should be stored. Mimepull would not be able to delete it after
        // it's moved.
        this.file = file;
    }
}
