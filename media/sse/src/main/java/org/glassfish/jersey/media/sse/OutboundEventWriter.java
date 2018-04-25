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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.sse.OutboundSseEvent;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.MessageUtils;

/**
 * Writer for {@link OutboundSseEvent}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class OutboundEventWriter implements MessageBodyWriter<OutboundSseEvent> {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    // encoding does not matter (lower ASCII characters)
    private static final byte[] COMMENT_LEAD = ": ".getBytes(UTF8);
    private static final byte[] NAME_LEAD = "event: ".getBytes(UTF8);
    private static final byte[] ID_LEAD = "id: ".getBytes(UTF8);
    private static final byte[] RETRY_LEAD = "retry: ".getBytes(UTF8);
    private static final byte[] DATA_LEAD = "data: ".getBytes(UTF8);
    private static final byte[] EOL = {'\n'};

    @Inject
    private Provider<MessageBodyWorkers> workersProvider;

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return OutboundSseEvent.class.isAssignableFrom(type) && SseFeature.SERVER_SENT_EVENTS_TYPE.isCompatible(mediaType);
    }

    @Override
    public long getSize(final OutboundSseEvent incomingEvent,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void writeTo(final OutboundSseEvent outboundEvent,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {

        final Charset charset = MessageUtils.getCharset(mediaType);
        if (outboundEvent.getComment() != null) {
            for (final String comment : outboundEvent.getComment().split("\n")) {
                entityStream.write(COMMENT_LEAD);
                entityStream.write(comment.getBytes(charset));
                entityStream.write(EOL);
            }
        }

        if (outboundEvent.getType() != null) {
            if (outboundEvent.getName() != null) {
                entityStream.write(NAME_LEAD);
                entityStream.write(outboundEvent.getName().getBytes(charset));
                entityStream.write(EOL);
            }
            if (outboundEvent.getId() != null) {
                entityStream.write(ID_LEAD);
                entityStream.write(outboundEvent.getId().getBytes(charset));
                entityStream.write(EOL);
            }
            if (outboundEvent.getReconnectDelay() > SseFeature.RECONNECT_NOT_SET) {
                entityStream.write(RETRY_LEAD);
                entityStream.write(Long.toString(outboundEvent.getReconnectDelay()).getBytes(charset));
                entityStream.write(EOL);
            }

            final MediaType eventMediaType =
                    outboundEvent.getMediaType() == null ? MediaType.TEXT_PLAIN_TYPE : outboundEvent.getMediaType();
            final MessageBodyWriter messageBodyWriter = workersProvider.get().getMessageBodyWriter(outboundEvent.getType(),
                    outboundEvent.getGenericType(), annotations, eventMediaType);
            messageBodyWriter.writeTo(
                    outboundEvent.getData(),
                    outboundEvent.getType(),
                    outboundEvent.getGenericType(),
                    annotations,
                    eventMediaType,
                    httpHeaders,
                    new OutputStream() {

                        private boolean start = true;

                        @Override
                        public void write(final int i) throws IOException {
                            if (start) {
                                entityStream.write(DATA_LEAD);
                                start = false;
                            }
                            entityStream.write(i);
                            if (i == '\n') {
                                entityStream.write(DATA_LEAD);
                            }
                        }
                    });
            entityStream.write(EOL);
        }
    }
}
