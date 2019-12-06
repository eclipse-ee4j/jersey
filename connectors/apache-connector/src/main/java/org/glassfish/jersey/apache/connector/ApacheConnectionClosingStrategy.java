/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.apache.connector;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.glassfish.jersey.client.ClientRequest;

import java.io.IOException;
import java.io.InputStream;

/**
 * /**
 * Strategy that defines the way the Apache client releases resources. The client enables closing the content stream
 * and the response. From the Apache documentation:
 * <pre>
 *     The difference between closing the content stream and closing the response is that
 *     the former will attempt to keep the underlying connection alive by consuming the
 *     entity content while the latter immediately shuts down and discards the connection.
 * </pre>
 * With Apache Client before 4.5.1, it was ok to close the response and the content stream. This is the default for
 * Apache Client 4.5 and older.
 * <p/>
 * For Apache Client 4.5.1+, first the content stream and the response is should be closed.
 * <p/>
 * In the case of Chunk content stream, the stream is not closed on the server side, and the client can hung on reading
 * the closing chunk. Using the {@link org.glassfish.jersey.client.ClientProperties#READ_TIMEOUT} property can prevent
 * this hanging forever and the reading of the closing chunk is terminated when the time is out. The other option, when
 * the timeout is not set, is to abort the Apache client request. This is the default for Apache Client 4.5.1+ when the
 * read timeout is not set.
 * <p/>
 * Another option is not to close the content stream, which is possible by the Apache client documentation. In this case,
 * however, the server side may not be notified and would not not close its chunk stream.
 */
public interface ApacheConnectionClosingStrategy {
    /**
     * Method to close the connection.
     * @param clientRequest The {@link ClientRequest} to get {@link ClientRequest#getConfiguration() configuration},
     *                      and {@link ClientRequest#resolveProperty(String, Class) resolve properties}.
     * @param request Apache {@code HttpUriRequest} that can be {@code abort}ed.
     * @param response Apache {@code CloseableHttpResponse} that can be {@code close}d.
     * @param stream The entity stream that can be {@link InputStream#close() closed}.
     * @throws IOException In case of some of the closing methods throws {@link IOException}
     */
    void close(ClientRequest clientRequest, HttpUriRequest request, CloseableHttpResponse response, InputStream stream)
            throws IOException;

    /**
     * Strategy that aborts Apache HttpRequests for the case of Chunked Stream, closes the stream, and response next.
     */
    class GracefulClosingStrategy implements ApacheConnectionClosingStrategy {
        static final GracefulClosingStrategy INSTANCE = new GracefulClosingStrategy();

        @Override
        public void close(ClientRequest clientRequest, HttpUriRequest request, CloseableHttpResponse response, InputStream stream)
                throws IOException {
            if (response.getEntity() != null && response.getEntity().isChunked()) {
                request.abort();
            }
            try {
                stream.close();
            } catch (IOException ex) {
                // Ignore
            } finally {
                response.close();
            }
        }
    }

    /**
     * Strategy that closes the response and content stream next. This is a behaviour of Jersey 2.28.
     */
    class ImmediateClosingStrategy implements ApacheConnectionClosingStrategy {
        static final ImmediateClosingStrategy INSTANCE = new ImmediateClosingStrategy();

        @Override
        public void close(ClientRequest clientRequest, HttpUriRequest request, CloseableHttpResponse response, InputStream stream)
                throws IOException {
            response.close();
            stream.close();
        }
    }
}
