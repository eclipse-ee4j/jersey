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

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.spi.ContentEncoder;

/**
 * GZIP encoding support. Interceptor that encodes the output or decodes the input if
 * {@link HttpHeaders#CONTENT_ENCODING Content-Encoding header} value equals to {@code gzip} or {@code x-gzip}.
 *
 * @author Martin Matula
 */
@Priority(Priorities.ENTITY_CODER)
public class GZipEncoder extends ContentEncoder {
    /**
     * Initialize GZipEncoder.
     */
    public GZipEncoder() {
        super("gzip", "x-gzip");
    }

    @Override
    public InputStream decode(String contentEncoding, InputStream encodedStream)
            throws IOException {
        return new GZIPInputStream(encodedStream);
    }

    @Override
    public OutputStream encode(String contentEncoding, OutputStream entityStream)
            throws IOException {
        return new GZIPOutputStream(entityStream);
    }
}
