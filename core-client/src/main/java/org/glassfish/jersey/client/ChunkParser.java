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

package org.glassfish.jersey.client;

import java.io.IOException;
import java.io.InputStream;

/**
 * Chunk data parser.
 *
 * Implementations of this interface are used by a {@link org.glassfish.jersey.client.ChunkedInput}
 * instance for parsing response entity input stream into chunks.
 * <p>
 * Chunk parsers are expected to read data from the response entity input stream
 * until a non-empty data chunk is fully read and then return the chunk data back
 * to the {@link org.glassfish.jersey.client.ChunkedInput} instance for further
 * processing (i.e. conversion into a specific Java type).
 * </p>
 * <p>
 * Chunk parsers are typically expected to skip any empty chunks (the chunks that do
 * not contain any data) or any control meta-data associated with chunks, however it
 * is not a hard requirement to do so. The decision depends on the knowledge of which
 * {@link javax.ws.rs.ext.MessageBodyReader} implementation is selected for de-serialization
 * of the chunk data.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface ChunkParser {
    /**
     * Invoked by {@link org.glassfish.jersey.client.ChunkedInput} to get the data for
     * the next chunk.
     *
     * @param responseStream response entity input stream.
     * @return next chunk data represented as an array of bytes, or {@code null}
     *         if no more chunks are available.
     * @throws java.io.IOException in case reading from the response entity fails.
     */
    public byte[] readChunk(InputStream responseStream) throws IOException;
}
