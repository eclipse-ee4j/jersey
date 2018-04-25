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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Configuration;

import javax.inject.Inject;

import org.glassfish.jersey.spi.ContentEncoder;

/**
 * Deflate encoding support. Interceptor that encodes the output or decodes the input if
 * {@link javax.ws.rs.core.HttpHeaders#CONTENT_ENCODING Content-Encoding header} value equals to {@code deflate}.
 * The default behavior of this interceptor can be tweaked using {@link MessageProperties#DEFLATE_WITHOUT_ZLIB}
 * property.
 *
 * @author Martin Matula
 */
@Priority(Priorities.ENTITY_CODER)
public class DeflateEncoder extends ContentEncoder {

    // TODO This provider should be registered and configured via a feature.
    private final Configuration config;

    /**
     * Initialize DeflateEncoder.
     *
     * @param config Jersey configuration properties.
     */
    @Inject
    public DeflateEncoder(final Configuration config) {
        super("deflate");
        this.config = config;
    }

    @Override
    public InputStream decode(String contentEncoding, InputStream encodedStream)
            throws IOException {
        // correct impl. should wrap deflate in zlib, but some don't do it - have to identify, which one we got
        InputStream markSupportingStream = encodedStream.markSupported() ? encodedStream
                : new BufferedInputStream(encodedStream);

        markSupportingStream.mark(1);
        // read the first byte
        int firstByte = markSupportingStream.read();
        markSupportingStream.reset();

        // if using zlib, first 3 bits should be 0, 4th should be 1
        // that should never be the case if no zlib wrapper
        if ((firstByte & 15) == 8) {
            // ok, zlib wrapped stream
            return new InflaterInputStream(markSupportingStream);
        } else {
            // no zlib wrapper
            return new InflaterInputStream(markSupportingStream, new Inflater(true));
        }
    }

    @Override
    public OutputStream encode(String contentEncoding, OutputStream entityStream)
            throws IOException {
        // some implementations don't support the correct deflate
        // so we have a property to configure the incorrect deflate (no zlib wrapper) should be used
        // let's check that
        Object value = config.getProperty(MessageProperties.DEFLATE_WITHOUT_ZLIB);
        boolean deflateWithoutZLib;
        if (value instanceof String) {
            deflateWithoutZLib = Boolean.valueOf((String) value);
        } else if (value instanceof Boolean) {
            deflateWithoutZLib = (Boolean) value;
        } else {
            deflateWithoutZLib = false;
        }

        return deflateWithoutZLib
                ? new DeflaterOutputStream(entityStream, new Deflater(Deflater.DEFAULT_COMPRESSION, true))
                : new DeflaterOutputStream(entityStream);
    }
}
