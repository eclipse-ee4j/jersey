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

import org.glassfish.jersey.media.multipart.file.DefaultMediaTypePredictor.CommonMediaTypes;

/**
 * An interface which allows developers implement their own media type predictor.
 *
 * @author Imran M Yousuf (imran at smartitengineering.com)
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public interface MediaTypePredictor {

    /**
     * Get the media type from a file name.
     *
     * @param file the file from which to get the {@link MediaType}.
     * @return the {@link MediaType} for the give file; {@code null} - if file
     *         is null; "application/octet-stream" if extension not recognized.
     *
     * @see CommonMediaTypes#getMediaTypeFromFileName(java.lang.String)
     */
    public MediaType getMediaTypeFromFile(final File file);

    /**
     * Get the media type from a file name. If the file
     * name extension is not recognised it will return {@link MediaType} for
     * "*\/*", it will also return the same if the file is {@code null}.
     *
     * @param fileName the file name from which to get the {@link MediaType}.
     * @return the {@link MediaType} for the give file; {@code null} - if file
     *         is null; "application/octet-stream" if extension not recognized.
     */
    public MediaType getMediaTypeFromFileName(final String fileName);

}
