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

package org.glassfish.jersey.media.sse;

import java.io.InputStream;
import java.lang.annotation.Annotation;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.client.ChunkParser;
import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.message.MessageBodyWorkers;

/**
 * Inbound Server-Sent Events channel.
 *
 * The input channel lets you serially read & consume SSE events as they arrive.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class EventInput extends ChunkedInput<InboundEvent> {
    /**
     * SSE event chunk parser - SSE chunks are delimited with a fixed "\n\n" and "\r\n\r\n" delimiter in the response stream.
     */
    private static final ChunkParser SSE_EVENT_PARSER = ChunkedInput.createMultiParser("\n\n", "\r\n\r\n");

    /**
     * Package-private constructor used by the {@link org.glassfish.jersey.client.ChunkedInputReader}.
     *
     * @param inputStream        response input stream.
     * @param annotations        annotations associated with response entity.
     * @param mediaType          response entity media type.
     * @param headers            response headers.
     * @param messageBodyWorkers message body workers.
     * @param propertiesDelegate properties delegate for this request/response.
     */
    EventInput(InputStream inputStream,
               Annotation[] annotations,
               MediaType mediaType,
               MultivaluedMap<String, String> headers,
               MessageBodyWorkers messageBodyWorkers,
               PropertiesDelegate propertiesDelegate) {
        super(InboundEvent.class, inputStream, annotations, mediaType, headers, messageBodyWorkers, propertiesDelegate);

        super.setParser(SSE_EVENT_PARSER);
    }
}
